package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.*;

public class CircuitBuilder {
    private final List<Circuit.PlacedLeafcell> cells = new ArrayList<>();
    private final Map<PinReference, NetReference> pins = new HashMap<>();
    private final Set<NetReference> existingNets = new HashSet<>();
    private final Set<NetReference> analogNets = new HashSet<>();
    private final Set<NetReference> inputNets = new HashSet<>();
    private final Set<NetReference> openDelayedNets = new HashSet<>();

    public static CircuitBuilder builder() {
        return new CircuitBuilder();
    }

    public CellBuilder addCell(LeafcellInstance<?, ?> cell, Vec2i pos) {
        return new CellBuilder(cell, pos);
    }

    public CircuitBuilder addInputNet(NetReference input, SignalType type) {
        inputNets.add(input);
        addNet(input, type);
        return this;
    }

    public CircuitBuilder addDelayedNet(NetReference delayed, SignalType type) {
        addNet(delayed, type);
        openDelayedNets.add(delayed);
        return this;
    }

    private void addNet(NetReference net, SignalType type) {
        Preconditions.checkState(!existingNets.contains(net));
        existingNets.add(net);
        if (type == SignalType.ANALOG) {
            analogNets.add(net);
        }
    }

    public Circuit build() {
        Preconditions.checkState(openDelayedNets.isEmpty());
        return new Circuit(cells, inputNets, pins);
    }

    public class CellBuilder {
        private final LeafcellInstance<?, ?> cell;
        private Vec2i pos;
        private final Map<PinReference, NetReference> cellPins = new HashMap<>();
        private final Set<NetReference> outputNets = new HashSet<>();

        public CellBuilder(LeafcellInstance<?, ?> cell, Vec2i pos) {
            this.cell = cell;
            this.pos = pos;
        }

        public CellBuilder output(String name, NetReference net) {
            Preconditions.checkState(!existingNets.contains(net) || openDelayedNets.contains(net));
            Preconditions.checkState(!outputNets.contains(net));
            Preconditions.checkState(!cellPins.containsValue(net));
            Preconditions.checkState(cell.getType().getOutputPins().containsKey(name));
            cellPins.put(new PinReference(cells.size(), true, name), net);
            outputNets.add(net);
            return this;
        }

        public CellBuilder input(String name, NetReference net) {
            Preconditions.checkState(existingNets.contains(net));
            final Pin cellPin = cell.getType().getInputPins().get(name);
            Preconditions.checkNotNull(cellPin);
            if (cellPin.type() == SignalType.DIGITAL) {
                Preconditions.checkState(!analogNets.contains(net));
            }
            cellPins.put(new PinReference(cells.size(), false, name), net);
            return this;
        }

        public CircuitBuilder buildCell() {
            final long numConnectedInputs = cellPins.keySet().stream()
                    .filter(p -> !p.isOutput())
                    .count();
            Preconditions.checkState(numConnectedInputs == cell.getType().getInputPins().size());
            cells.add(new Circuit.PlacedLeafcell(cell, pos));
            pins.putAll(cellPins);
            for (Map.Entry<PinReference, NetReference> e : cellPins.entrySet()) {
                if (e.getKey().isOutput()) {
                    final Pin cellPin = cell.getType().getOutputPins().get(e.getKey().pinName());
                    if (cellPin.direction().isCombinatorialOutput() || !openDelayedNets.contains(e.getValue())) {
                        addNet(e.getValue(), cellPin.type());
                    } else {
                        openDelayedNets.remove(e.getValue());
                    }
                }
            }
            return CircuitBuilder.this;
        }
    }
}
