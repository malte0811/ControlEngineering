package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.circuit.CircuitBuilder;
import malte0811.controlengineering.logic.circuit.CircuitBuilder.StageBuilder;
import malte0811.controlengineering.logic.circuit.CircuitBuilder.StageBuilder.CellBuilder;
import malte0811.controlengineering.logic.circuit.NetReference;
import malte0811.controlengineering.logic.schematic.SchematicNet.ConnectedPin;
import malte0811.controlengineering.logic.schematic.symbol.*;
import malte0811.controlengineering.util.Vec2d;
import malte0811.controlengineering.util.Vec2i;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class Schematic {
    public static final Codec<Schematic> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.list(PlacedSymbol.CODEC).fieldOf("symbols").forGetter(s -> s.symbols),
                    Codec.list(SchematicNet.CODEC).fieldOf("nets").forGetter(s -> s.nets)
            ).apply(inst, Schematic::new)
    );

    private final List<PlacedSymbol> symbols;
    private final List<SchematicNet> nets;

    private Schematic(List<PlacedSymbol> symbols, List<SchematicNet> nets) {
        this.symbols = new ArrayList<>(symbols);
        this.nets = new ArrayList<>(nets);
    }

    public Schematic() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
    }

    public boolean canPlace(PlacedSymbol candidate) {
        if (!symbols.stream().allMatch(candidate::canCoexist)) {
            // Intersects with other symbol(s)
            return false;
        }
        for (SchematicNet net : nets) {
            Set<ConnectedPin> pinsInNet = net.getConnectedPins(symbols);
            pinsInNet.addAll(net.getConnectedPins(Collections.singletonList(candidate)));
            if (!ConnectedPin.isConsistent(pinsInNet)) {
                // Would make a net inconsistent, i.e. would add multiple sources or analog/digital incompatibility
                return false;
            }
        }
        return true;
    }

    public void addWire(WireSegment segment) {
        IntSet connectedIndices = getConnectedIndices(segment);
        if (connectedIndices.isEmpty()) {
            // New net
            nets.add(new SchematicNet(ImmutableList.of(segment)));
        } else if (connectedIndices.size() == 1) {
            // Within one net
            nets.get(connectedIndices.iterator().nextInt()).addSegment(segment);
        } else {
            // connecting two nets
            Preconditions.checkState(connectedIndices.size() == 2);
            IntIterator it = connectedIndices.iterator();
            final SchematicNet netToKeep = nets.get(it.nextInt());
            final SchematicNet netToRemove = nets.remove(it.nextInt());
            netToKeep.addAll(netToRemove);
            netToKeep.addSegment(segment);
        }
    }

    public boolean canAdd(WireSegment segment) {
        Set<ConnectedPin> pinsOnWire = new SchematicNet(
                Collections.singletonList(segment)
        ).getConnectedPins(symbols);
        if (!ConnectedPin.isConsistent(pinsOnWire)) {
            return false;
        }
        IntSet netsToCheck = getConnectedIndices(segment);
        for (int netIdA : netsToCheck) {
            SchematicNet netA = nets.get(netIdA);
            for (int netIdB : netsToCheck) {
                if (netIdB > netIdA) {
                    SchematicNet netB = nets.get(netIdA);
                    if (!netA.canMerge(netB, symbols)) {
                        return false;
                    }
                }
            }
            if (!netA.canAdd(segment, symbols)) {
                return false;
            }
        }
        return true;
    }

    public boolean removeOneContaining(Vec2d mouse) {
        for (Iterator<SchematicNet> iterator = nets.iterator(); iterator.hasNext(); ) {
            SchematicNet net = iterator.next();
            if (net.removeOneContaining(mouse.floor())) {
                iterator.remove();
                nets.addAll(net.splitComponents());
                return true;
            }
        }
        for (Iterator<PlacedSymbol> iterator = symbols.iterator(); iterator.hasNext(); ) {
            if (iterator.next().containsPoint(mouse)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private IntSet getConnectedIndices(WireSegment toAdd) {
        IntSet indices = new IntArraySet();
        for (int i = 0; i < nets.size(); ++i) {
            final SchematicNet net = nets.get(i);
            for (Vec2i end : toAdd.getEnds()) {
                if (net.contains(end)) {
                    indices.add(i);
                }
            }
        }
        return indices;
    }

    public void render(MatrixStack stack) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
        // TODO highlight nets under cursor?
        for (SchematicNet net : nets) {
            net.render(stack);
        }
    }

    @Nullable
    public PlacedSymbol getSymbolAt(Vec2d pos) {
        for (PlacedSymbol symbol : symbols) {
            if (symbol.containsPoint(pos)) {
                return symbol;
            }
        }
        return null;
    }

    public Either<BusConnectedCircuit, List<SchematicError>> toCircuit() {
        Map<NetReference, List<BusSignalRef>> outputConnections = new HashMap<>();
        Map<BusSignalRef, List<NetReference>> inputConnections = new HashMap<>();
        Map<ConnectedPin, NetReference> cellPins = new HashMap<>();
        BooleanList netHasSource = new BooleanArrayList();
        netHasSource.size(nets.size());
        for (int netId = 0; netId < nets.size(); netId++) {
            SchematicNet net = nets.get(netId);
            NetReference netRef = new NetReference(netId + "");
            for (ConnectedPin pin : net.getConnectedPins(symbols)) {
                SymbolInstance<?> symbolInstance = pin.getSymbol().getSymbol();
                SchematicSymbol<?> symbolType = symbolInstance.getType();
                if (symbolType instanceof CellSymbol) {
                    cellPins.put(pin, netRef);
                } else if (symbolType instanceof IOSymbol) {
                    BusSignalRef busRef = (BusSignalRef) symbolInstance.getCurrentState();
                    if (((IOSymbol) symbolType).isInput()) {
                        inputConnections.computeIfAbsent(busRef, $ -> new ArrayList<>()).add(netRef);
                    } else {
                        outputConnections.computeIfAbsent(netRef, $ -> new ArrayList<>()).add(busRef);
                    }
                }
                if (pin.isOutput()) {
                    netHasSource.set(netId, true);
                }
            }
        }

        CircuitBuilder builder = CircuitBuilder.builder();
        builder.addInputNet(BusConnectedCircuit.ONE, SignalType.DIGITAL);
        builder.addInputNet(BusConnectedCircuit.ZERO, SignalType.DIGITAL);
        inputConnections.values().stream()
                .flatMap(List::stream)
                .forEach(ref -> builder.addInputNet(ref, SignalType.ANALOG));
        StageBuilder currentStage = null;
        List<PlacedSymbol> cells = symbols.stream()
                .filter(s -> s.getSymbol().getType() instanceof CellSymbol)
                .sorted(Comparator.comparing(PlacedSymbol::getPosition))
                .collect(Collectors.toList());
        int lastX = Integer.MIN_VALUE;
        for (PlacedSymbol cell : cells) {
            if (currentStage != null && cell.getPosition().x != lastX) {
                currentStage.buildStage();
                currentStage = null;
            }
            if (currentStage == null) {
                currentStage = builder.addStage();
                lastX = cell.getPosition().x;
            }
            CellSymbol symbol = (CellSymbol) cell.getSymbol().getType();
            CellBuilder cellBuilder = currentStage.addCell(symbol.getCellType().newInstance());
            List<SymbolPin> inputPins = symbol.getInputPins();
            for (int i = 0; i < inputPins.size(); i++) {
                NetReference inputNet = cellPins.get(new ConnectedPin(cell, inputPins.get(i), false));
                if (inputNet == null || !netHasSource.getBoolean(i)) {
                    cellBuilder.input(i, BusConnectedCircuit.ZERO);
                    //TODO add error
                } else {
                    cellBuilder.input(i, inputNet);
                }
            }
            List<SymbolPin> outputPins = symbol.getOutputPins();
            for (int i = 0; i < outputPins.size(); i++) {
                cellBuilder.output(i, cellPins.get(new ConnectedPin(cell, outputPins.get(i), true)));
            }
            cellBuilder.buildCell();
        }
        return Either.left(new BusConnectedCircuit(builder.build(), outputConnections, inputConnections));
    }
}
