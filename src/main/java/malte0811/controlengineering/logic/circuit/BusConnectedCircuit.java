package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.logic.cells.impl.Digitizer;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class BusConnectedCircuit {
    private static final MyCodec<Map<NetReference, List<BusSignalRef>>> OUTPUT_CONN_CODEC =
            MyCodecs.codecForMap(NetReference.CODEC, MyCodecs.list(BusSignalRef.CODEC));
    private static final MyCodec<Map<NetReference, Double>> CONSTANTS_CODEC =
            MyCodecs.codecForMap(NetReference.CODEC, MyCodecs.DOUBLE);
    public static final MyCodec<BusConnectedCircuit> CODEC = new RecordCodec3<>(
            new CodecField<>("circuit", b -> b.circuit, Circuit.CODEC),
            new CodecField<>("outputs", b -> b.outputConnections, OUTPUT_CONN_CODEC),
            new CodecField<>("inputs", b -> b.inputConnections, MyCodecs.list(InputConnection.CODEC)),
            BusConnectedCircuit::new
    );

    private final Circuit circuit;
    private final Map<NetReference, List<BusSignalRef>> outputConnections;
    private final List<InputConnection> inputConnections;
    private BusState outputValues = BusState.EMPTY;

    public BusConnectedCircuit(
            Circuit circuit,
            Map<NetReference, List<BusSignalRef>> outputConnections,
            List<InputConnection> inputConnections
    ) {
        this.circuit = circuit;
        this.outputConnections = outputConnections;
        this.inputConnections = inputConnections;
        propagateToOutputs();
    }

    public BusConnectedCircuit(
            Circuit circuit,
            Map<NetReference, List<BusSignalRef>> outputConnections,
            List<InputConnection> inputConnections,
            Map<NetReference, Integer> constantInputs
    ) {
        this(circuit, outputConnections, inputConnections);
        Preconditions.checkArgument(
                inputConnections.stream()
                        .map(InputConnection::connectedNets)
                        .flatMap(List::stream)
                        .noneMatch(constantInputs::containsKey)
        );
        constantInputs.forEach(circuit::updateInputValue);
        propagateToOutputs();
    }

    public void updateInputs(BusState bus) {
        for (var input : inputConnections) {
            final var rawValue = bus.getSignal(input.busSignal());
            final var realValue = input.digitized() ? Digitizer.digitize(rawValue) : rawValue;
            for (NetReference circuitNet : input.connectedNets()) {
                circuit.updateInputValue(circuitNet, realValue);
            }
        }
    }

    public boolean tick() {
        circuit.tick();
        return propagateToOutputs();
    }

    private boolean propagateToOutputs() {
        boolean changed = false;
        for (Map.Entry<NetReference, List<BusSignalRef>> output : outputConnections.entrySet()) {
            final int newValue = Mth.clamp(
                    circuit.getNetValue(output.getKey()), BusLine.MIN_VALID_VALUE, BusLine.MAX_VALID_VALUE
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

    public record InputConnection(BusSignalRef busSignal, List<NetReference> connectedNets, boolean digitized) {
        public static final MyCodec<InputConnection> CODEC = new RecordCodec3<>(
                new CodecField<>("signal", InputConnection::busSignal, BusSignalRef.CODEC),
                new CodecField<>("nets", InputConnection::connectedNets, MyCodecs.list(NetReference.CODEC)),
                new CodecField<>("digitized", InputConnection::digitized, MyCodecs.BOOL),
                InputConnection::new
        );
    }
}
