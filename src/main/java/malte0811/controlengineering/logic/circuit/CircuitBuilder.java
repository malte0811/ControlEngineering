package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.logic.cells.LeafcellInstance;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;

import java.util.*;

public class CircuitBuilder {
    private final List<List<LeafcellInstance<?>>> stages = new ArrayList<>();
    private final Map<PinReference, NetReference> pins = new HashMap<>();
    private final Set<NetReference> existingNets = new HashSet<>();
    private final Set<NetReference> analogNets = new HashSet<>();
    private final Set<NetReference> inputNets = new HashSet<>();

    public static CircuitBuilder builder() {
        return new CircuitBuilder();
    }

    public StageBuilder addStage() {
        return new StageBuilder();
    }

    public CircuitBuilder addInputNet(NetReference input, SignalType type) {
        Preconditions.checkState(!existingNets.contains(input));
        existingNets.add(input);
        inputNets.add(input);
        if (type == SignalType.ANALOG) {
            analogNets.add(input);
        }
        return this;
    }

    public Circuit build() {
        return new Circuit(stages, inputNets, pins);
    }

    public class StageBuilder {
        private final List<LeafcellInstance<?>> cells = new ArrayList<>();
        private final Set<NetReference> outputNets = new HashSet<>();
        private final Set<NetReference> analogOutputNets = new HashSet<>();
        private final Map<PinReference, NetReference> pinsInStage = new HashMap<>();

        public CircuitBuilder buildStage() {
            stages.add(cells);
            pins.putAll(pinsInStage);
            existingNets.addAll(outputNets);
            analogNets.addAll(analogOutputNets);
            return CircuitBuilder.this;
        }

        public CellBuilder addCell(LeafcellInstance<?> cell) {
            return new CellBuilder(cell);
        }

        public class CellBuilder {
            private final LeafcellInstance<?> cell;
            private final Map<PinReference, NetReference> cellPins = new HashMap<>();

            public CellBuilder(LeafcellInstance<?> cell) {
                this.cell = cell;
            }

            public CellBuilder output(int index, NetReference net) {
                Preconditions.checkState(!existingNets.contains(net));
                Preconditions.checkState(!outputNets.contains(net));
                Preconditions.checkState(!cellPins.containsValue(net));
                Preconditions.checkState(index < cell.getType().getOutputPins().size());
                cellPins.put(new PinReference(stages.size(), cells.size(), true, index), net);
                return this;
            }

            public CellBuilder input(int index, NetReference net) {
                Preconditions.checkState(existingNets.contains(net));
                Preconditions.checkState(index < cell.getType().getInputPins().size());
                final Pin cellPin = cell.getType().getInputPins().get(index);
                if (cellPin.getType() == SignalType.DIGITAL) {
                    Preconditions.checkState(!analogNets.contains(net));
                }
                cellPins.put(new PinReference(stages.size(), cells.size(), false, index), net);
                return this;
            }

            public StageBuilder buildCell() {
                final long numConnectedInputs = cellPins.keySet().stream()
                        .filter(p -> !p.isOutput())
                        .count();
                Preconditions.checkState(numConnectedInputs == cell.getType().getInputPins().size());
                cells.add(cell);
                pinsInStage.putAll(cellPins);
                for (Map.Entry<PinReference, NetReference> e : cellPins.entrySet()) {
                    if (e.getKey().isOutput()) {
                        outputNets.add(e.getValue());
                        final Pin cellPin = cell.getType().getOutputPins().get(e.getKey().getPin());
                        if (cellPin.getType() == SignalType.ANALOG) {
                            analogOutputNets.add(e.getValue());
                        }
                    }
                }
                return StageBuilder.this;
            }
        }
    }
}
