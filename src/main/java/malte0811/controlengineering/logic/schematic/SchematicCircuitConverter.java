package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.circuit.CircuitBuilder;
import malte0811.controlengineering.logic.circuit.NetReference;
import malte0811.controlengineering.logic.schematic.symbol.*;
import net.minecraftforge.fml.loading.toposort.CyclePresentException;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SchematicCircuitConverter {
    private static Map<NetReference, List<ConnectedPin>> getPinsByNet(Schematic schematic) {
        Map<NetReference, List<ConnectedPin>> result = new HashMap<>();
        for (SchematicNet net : schematic.getNets()) {
            final Set<ConnectedPin> allPins = net.getOrComputePins(schematic.getSymbols());
            if (!allPins.isEmpty()) {
                List<ConnectedPin> asList = new ArrayList<>(allPins);
                result.put(new NetReference(asList.get(0).toString()), asList);
            }
        }
        return result;
    }

    @Nullable
    private static Pair<ConnectedPin, List<ConnectedPin>> splitSourceFromSinks(Collection<ConnectedPin> allPins) {
        Optional<ConnectedPin> source = getSource(allPins);
        if (!source.isPresent()) {
            return null;
        }
        return Pair.of(source.get(), getSinks(allPins));
    }

    public static Map<ConnectedPin, List<ConnectedPin>> getNetsBySource(
            Collection<? extends Collection<ConnectedPin>> nets
    ) {
        return nets.stream()
                .map(SchematicCircuitConverter::splitSourceFromSinks)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public static List<ConnectedPin> getFloatingInputs(Schematic schematic) {
        return getFloatingInputs(schematic.getSymbols(), getPinsByNet(schematic).values());
    }

    public static List<ConnectedPin> getFloatingInputs(
            List<PlacedSymbol> symbols, Collection<List<ConnectedPin>> nets
    ) {
        Set<ConnectedPin> pinsWithoutNet = new HashSet<>();
        for (PlacedSymbol symbol : symbols) {
            for (SymbolPin pin : symbol.getSymbol().getPins()) {
                pinsWithoutNet.add(new ConnectedPin(symbol, pin));
            }
        }
        for (List<ConnectedPin> net : nets) {
            if (getSource(net).isPresent()) {
                for (ConnectedPin pin : net) {
                    pinsWithoutNet.remove(pin);
                }
            }
        }
        pinsWithoutNet.removeIf(pin -> pin.pin().isOutput());
        return new ArrayList<>(pinsWithoutNet);
    }

    public static Optional<List<PlacedSymbol>> getCellOrder(Schematic schematic) {
        Map<NetReference, List<ConnectedPin>> nets = getPinsByNet(schematic);
        return getCellOrder(schematic.getSymbols(), getNetsBySource(nets.values()));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Optional<List<PlacedSymbol>> getCellOrder(
            List<PlacedSymbol> symbols, Map<ConnectedPin, List<ConnectedPin>> nets
    ) {
        MutableGraph<PlacedSymbol> graph = GraphBuilder.directed()
                .expectedNodeCount(symbols.size())
                .build();
        for (PlacedSymbol symbol : symbols) {
            graph.addNode(symbol);
        }
        for (Map.Entry<ConnectedPin, List<ConnectedPin>> net : nets.entrySet()) {
            if (!net.getKey().pin().isCombinatorialOutput()) {
                continue;
            }
            PlacedSymbol source = net.getKey().symbol();
            for (ConnectedPin sink : net.getValue()) {
                if (source == sink.symbol()) {
                    return Optional.empty();
                }
                graph.putEdge(source, sink.symbol());
            }
        }
        try {
            return Optional.of(TopologicalSort.topologicalSort(graph, null));
        } catch (CyclePresentException x) {
            return Optional.empty();
        }
    }

    private static Map<ConnectedPin, NetReference> toPinNetMap(Map<NetReference, List<ConnectedPin>> netsBySource) {
        Map<ConnectedPin, NetReference> result = new HashMap<>();
        for (Map.Entry<NetReference, List<ConnectedPin>> entry : netsBySource.entrySet()) {
            for (ConnectedPin sink : entry.getValue()) {
                result.put(sink, entry.getKey());
            }
        }
        return result;
    }

    private static Optional<ConnectedPin> getSource(Collection<ConnectedPin> netPins) {
        return netPins.stream().filter(p -> p.pin().isOutput()).findAny();
    }

    private static List<ConnectedPin> getSinks(Collection<ConnectedPin> netPins) {
        return netPins.stream()
                .filter(p -> !p.pin().isOutput())
                .collect(Collectors.toList());
    }

    private static <T> Map<NetReference, T> getNetsWithSource(
            Map<NetReference, List<ConnectedPin>> nets, SchematicSymbol<T> sourceType
    ) {
        Map<NetReference, T> result = new HashMap<>();
        for (Map.Entry<NetReference, List<ConnectedPin>> entry : nets.entrySet()) {
            Optional<ConnectedPin> source = getSource(entry.getValue());
            if (source.isPresent()) {
                SymbolInstance<?> symbol = source.get().symbol().getSymbol();
                if (symbol.getType() == sourceType) {
                    result.put(entry.getKey(), (T) symbol.getCurrentState());
                }
            }
        }
        return result;
    }

    private static Map<BusSignalRef, List<NetReference>> getInputConnections(Map<NetReference, List<ConnectedPin>> nets) {
        return Multimaps.asMap(
                getNetsWithSource(nets, SchematicSymbols.INPUT_PIN).entrySet().stream()
                        .collect(ImmutableListMultimap.toImmutableListMultimap(Map.Entry::getValue, Map.Entry::getKey))
        );
    }

    private static Map<NetReference, Double> getConstantNets(Map<NetReference, List<ConnectedPin>> nets) {
        return getNetsWithSource(nets, SchematicSymbols.CONSTANT);
    }

    private static Map<NetReference, List<BusSignalRef>> getOutputConnections(Map<NetReference, List<ConnectedPin>> nets) {
        Map<NetReference, List<BusSignalRef>> result = new HashMap<>();
        for (Map.Entry<NetReference, List<ConnectedPin>> entry : nets.entrySet()) {
            for (ConnectedPin sink : getSinks(entry.getValue())) {
                SymbolInstance<?> symbol = sink.symbol().getSymbol();
                SchematicSymbol<?> type = symbol.getType();
                if (type instanceof IOSymbol) {
                    result.computeIfAbsent(entry.getKey(), $ -> new ArrayList<>())
                            .add((BusSignalRef) symbol.getCurrentState());
                }
            }
        }
        return result;
    }

    public static Optional<BusConnectedCircuit> toCircuit(Schematic schematic) {
        final Map<NetReference, List<ConnectedPin>> nets = getPinsByNet(schematic);
        if (!getFloatingInputs(schematic.getSymbols(), nets.values()).isEmpty()) {
            return Optional.empty();
        }
        final Map<ConnectedPin, NetReference> pinsToNet = toPinNetMap(nets);
        final Map<NetReference, List<BusSignalRef>> outputConnections = getOutputConnections(nets);
        final Map<BusSignalRef, List<NetReference>> inputConnections = getInputConnections(nets);
        final Map<NetReference, Double> constantNets = getConstantNets(nets);
        CircuitBuilder builder = CircuitBuilder.builder();
        for (List<NetReference> netReferences : inputConnections.values()) {
            for (NetReference input : netReferences) {
                builder.addInputNet(input, SignalType.ANALOG);
            }
        }
        for (Map.Entry<NetReference, Double> entry : constantNets.entrySet()) {
            double value = entry.getValue();
            builder.addInputNet(entry.getKey(), value == 1 || value == 0 ? SignalType.DIGITAL : SignalType.ANALOG);
        }
        for (Map.Entry<NetReference, List<ConnectedPin>> net : nets.entrySet()) {
            Optional<ConnectedPin> source = getSource(net.getValue());
            if (source.isPresent() && !source.get().pin().isCombinatorialOutput()) {
                builder.addDelayedNet(net.getKey(), source.get().pin().type());
            }
        }

        Optional<List<PlacedSymbol>> order = getCellOrder(schematic.getSymbols(), getNetsBySource(nets.values()));
        if (!order.isPresent()) {
            return Optional.empty();
        }
        List<PlacedSymbol> cells = order.get().stream()
                .filter(s -> s.getSymbol().getType() instanceof CellSymbol)
                .collect(Collectors.toList());
        for (PlacedSymbol cell : cells) {
            SymbolInstance<?> instance = cell.getSymbol();
            CellSymbol symbol = (CellSymbol) instance.getType();
            CircuitBuilder.CellBuilder cellBuilder = builder.addCell(symbol.getCellType().newInstance());
            for (SymbolPin pin : instance.getPins()) {
                ConnectedPin connectedPin = new ConnectedPin(cell, pin);
                NetReference circuitNet = pinsToNet.get(connectedPin);
                if (pin.isOutput()) {
                    if (circuitNet != null) {
                        // Non-connected output is not an error
                        cellBuilder.output(pin.pinName(), circuitNet);
                    }
                } else {
                    cellBuilder.input(pin.pinName(), circuitNet);
                }
            }
            cellBuilder.buildCell();
        }
        return Optional.of(
                new BusConnectedCircuit(builder.build(), outputConnections, inputConnections, constantNets)
        );
    }
}
