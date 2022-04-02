package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.logic.cells.impl.Digitizer;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class BusConnectedCircuit {
    private final Circuit circuit;
    private final Map<NetReference, List<BusSignalRef>> outputConnections;
    private final List<InputConnection> inputConnections;
    private final Map<NetReference, Double> constantInputs;
    private BusState outputValues = BusState.EMPTY;

    public BusConnectedCircuit(
            Circuit circuit,
            Map<NetReference, List<BusSignalRef>> outputConnections,
            List<InputConnection> inputConnections,
            Map<NetReference, Double> constantInputs
    ) {
        this.circuit = circuit;
        this.outputConnections = outputConnections;
        this.inputConnections = inputConnections;
        this.constantInputs = constantInputs;
        Preconditions.checkArgument(
                inputConnections.stream()
                        .map(InputConnection::connectedNets)
                        .flatMap(List::stream)
                        .noneMatch(constantInputs::containsKey)
        );
        constantInputs.forEach(circuit::updateInputValue);
    }

    public void updateInputs(BusState bus) {
        for (var input : inputConnections) {
            final var rawValue = bus.getSignal(input.busSignal()) / (double) BusLine.MAX_VALID_VALUE;
            final var realValue = input.digitized() ? Digitizer.digitize(rawValue) : rawValue;
            for (NetReference circuitNet : input.connectedNets()) {
                circuit.updateInputValue(circuitNet, realValue);
            }
        }
    }

    public boolean tick() {
        circuit.tick();
        boolean changed = false;
        for (Map.Entry<NetReference, List<BusSignalRef>> output : outputConnections.entrySet()) {
            final int newValue = (int) Mth.clamp(
                    BusLine.MAX_VALID_VALUE * circuit.getNetValue(output.getKey()),
                    BusLine.MIN_VALID_VALUE,
                    BusLine.MAX_VALID_VALUE
            );
            for (BusSignalRef busSignal : output.getValue()) {
                final int currentValue = outputValues.getSignal(busSignal);
                if (currentValue != newValue) {
                    outputValues = outputValues.with(busSignal, newValue);
                    changed = true;
                }
            }
        }
        return changed;
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public BusState getOutputState() {
        return outputValues;
    }

    public record InputConnection(BusSignalRef busSignal, List<NetReference> connectedNets, boolean digitized) {}
}
