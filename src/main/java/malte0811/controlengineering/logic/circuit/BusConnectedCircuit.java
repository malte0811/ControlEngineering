package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.impl.Digitizer;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class BusConnectedCircuit {

    private static final MyCodec<Map<NetReference, List<BusSignalRef>>> OUTPUT_CODEC = MyCodecs.codecForMap(
            NetReference.CODEC, MyCodecs.list(BusSignalRef.CODEC)
    );
    private static final MyCodec<List<InputConnection>> INPUT_CODEC = MyCodecs.list(new RecordCodec3<>(
            // Names are for backward compatibility
            new CodecField<>("first", InputConnection::busSignal, BusSignalRef.CODEC),
            new CodecField<>("second", InputConnection::connectedNets, MyCodecs.list(NetReference.CODEC)),
            new CodecField<>("digital", InputConnection::digitized, MyCodecs.BOOL),
            InputConnection::new
    ));
    private static final MyCodec<Map<NetReference, Double>> CONSTANCE_CODEC = MyCodecs.codecForMap(
            NetReference.CODEC, MyCodecs.DOUBLE
    );
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

    public BusConnectedCircuit(CompoundTag nbt) {
        this(
                Circuit.fromNBT(nbt.getCompound("circuit")),
                OUTPUT_CODEC.fromNBT(nbt.get("outputs")),
                INPUT_CODEC.fromNBT(nbt.get("inputs")),
                CONSTANCE_CODEC.fromNBT(nbt.get("constants"))
        );
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

    public CompoundTag toNBT() {
        CompoundTag result = new CompoundTag();
        result.put("circuit", circuit.toNBT());
        result.put("inputs", INPUT_CODEC.toNBT(inputConnections));
        result.put("outputs", OUTPUT_CODEC.toNBT(outputConnections));
        result.put("constants", CONSTANCE_CODEC.toNBT(constantInputs));
        return result;
    }

    public BusState getOutputState() {
        return outputValues;
    }

    private int getTotalCost(ToDoubleFunction<CellCost> individualCost) {
        return Mth.ceil(getCircuit()
                .getCellTypes()
                .map(LeafcellType::getCost)
                .mapToDouble(individualCost)
                .sum());
    }

    public int getNumTubes() {
        return getTotalCost(CellCost::numTubes);
    }

    public int getWireLength() {
        return getTotalCost(CellCost::wireLength);
    }

    public int getSolderAmount() {
        return getTotalCost(CellCost::getSolderAmount);
    }

    public record InputConnection(BusSignalRef busSignal, List<NetReference> connectedNets, boolean digitized) {}
}
