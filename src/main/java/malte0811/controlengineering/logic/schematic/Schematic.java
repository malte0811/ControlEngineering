package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
        this.resetConnectedPins();
    }

    public Schematic() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
        resetConnectedPins();
    }

    public boolean canPlace(PlacedSymbol candidate) {
        if (!symbols.stream().allMatch(candidate::canCoexist)) {
            // Intersects with other symbol(s)
            return false;
        }
        for (SchematicNet net : this.nets) {
            Set<ConnectedPin> pinsInNet = new HashSet<>(net.getOrComputePins(symbols));
            pinsInNet.addAll(net.computeConnectedPins(Collections.singletonList(candidate)));
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
            nets.add(new SchematicNet(segment));
        } else if (connectedIndices.size() == 1) {
            // Within one net
            final int netId = connectedIndices.iterator().nextInt();
            nets.get(netId).addSegment(segment);
        } else {
            // connecting two nets
            Preconditions.checkState(connectedIndices.size() == 2);
            IntIterator it = connectedIndices.iterator();
            final int netIdToKeep = it.nextInt();
            final int netIdToRemove = it.nextInt();
            final SchematicNet netToKeep = nets.get(netIdToKeep);
            final SchematicNet netToRemove = nets.remove(netIdToRemove);
            netToKeep.addAll(netToRemove);
            netToKeep.addSegment(segment);
        }
        resetConnectedPins();
    }

    public boolean canAdd(WireSegment segment) {
        Set<ConnectedPin> pinsOnWire = new SchematicNet(segment).computeConnectedPins(symbols);
        if (!ConnectedPin.isConsistent(pinsOnWire)) {
            return false;
        }
        IntSet netsToCheck = getConnectedIndices(segment);
        for (int netIdA : netsToCheck) {
            SchematicNet netA = nets.get(netIdA);
            for (int netIdB : netsToCheck) {
                if (netIdB > netIdA) {
                    SchematicNet netB = nets.get(netIdB);
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
                resetConnectedPins();
                return true;
            }
        }
        for (Iterator<PlacedSymbol> iterator = symbols.iterator(); iterator.hasNext(); ) {
            if (iterator.next().containsPoint(mouse)) {
                iterator.remove();
                resetConnectedPins();
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

    public void render(MatrixStack stack, Vec2d mouse) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
        for (SchematicNet net : nets) {
            net.render(stack, mouse, symbols);
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

    private void resetConnectedPins() {
        nets.forEach(SchematicNet::resetCachedPins);
    }

    //TODO split up or maybe move somewhere else
    public Either<BusConnectedCircuit, List<ConnectedPin>> toCircuit() {
        Map<NetReference, List<BusSignalRef>> outputConnections = new HashMap<>();
        Map<BusSignalRef, List<NetReference>> inputConnections = new HashMap<>();
        Map<ConnectedPin, NetReference> cellPins = new HashMap<>();
        Map<NetReference, Double> constantNets = new HashMap<>();
        Set<NetReference> netHasSource = new HashSet<>();
        NetReference errorNet = null;
        List<ConnectedPin> errors = new ArrayList<>();
        CircuitBuilder builder = CircuitBuilder.builder();
        for (int netId = 0; netId < nets.size(); netId++) {
            SchematicNet net = nets.get(netId);
            NetReference netRef = new NetReference(netId + "");
            for (ConnectedPin pin : net.getOrComputePins(symbols)) {
                SymbolInstance<?> symbolInstance = pin.getSymbol().getSymbol();
                SchematicSymbol<?> symbolType = symbolInstance.getType();
                if (symbolType instanceof CellSymbol) {
                    cellPins.put(pin, netRef);
                } else if (symbolType instanceof IOSymbol) {
                    BusSignalRef busRef = (BusSignalRef) symbolInstance.getCurrentState();
                    if (((IOSymbol) symbolType).isInput()) {
                        inputConnections.computeIfAbsent(busRef, $ -> new ArrayList<>()).add(netRef);
                        builder.addInputNet(netRef, SignalType.ANALOG);
                    } else {
                        outputConnections.computeIfAbsent(netRef, $ -> new ArrayList<>()).add(busRef);
                    }
                } else if (symbolType instanceof ConstantSymbol) {
                    constantNets.put(netRef, (Double) symbolInstance.getCurrentState());
                    builder.addInputNet(netRef, pin.getPin().getType());
                }
                if (pin.getPin().isOutput()) {
                    netHasSource.add(netRef);
                }
            }
        }

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
            SymbolInstance<?> instance = cell.getSymbol();
            CellSymbol symbol = (CellSymbol) instance.getType();
            CellBuilder cellBuilder = currentStage.addCell(symbol.getCellType().newInstance());
            int inputIndex = 0;
            int outputIndex = 0;
            for (SymbolPin pin : instance.getPins()) {
                ConnectedPin connectedPin = new ConnectedPin(cell, pin);
                NetReference circuitNet = cellPins.get(connectedPin);
                if (pin.isOutput()) {
                    if (circuitNet != null) {
                        // Non-connected output is not an error
                        cellBuilder.output(outputIndex, circuitNet);
                    }
                    ++outputIndex;
                } else {
                    if (circuitNet == null || !netHasSource.contains(circuitNet)) {
                        if (errorNet == null) {
                            errorNet = new NetReference("error");
                            builder.addInputNet(errorNet, SignalType.DIGITAL);
                        }
                        cellBuilder.input(inputIndex, errorNet);
                        errors.add(connectedPin);
                    } else {
                        cellBuilder.input(inputIndex, circuitNet);
                    }
                    ++inputIndex;
                }
            }
            cellBuilder.buildCell();
        }
        if (currentStage != null) {
            currentStage.buildStage();
        }
        if (errors.isEmpty()) {
            return Either.left(
                    new BusConnectedCircuit(builder.build(), outputConnections, inputConnections, constantNets)
            );
        } else {
            return Either.right(errors);
        }
    }
}
