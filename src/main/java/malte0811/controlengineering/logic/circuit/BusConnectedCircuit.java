package malte0811.controlengineering.logic.circuit;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class BusConnectedCircuit {

    private static final Codec<Map<NetReference, List<BusSignalRef>>> OUTPUT_CODEC = Codecs.codecForMap(
            NetReference.CODEC, Codec.list(BusSignalRef.CODEC)
    );
    private static final Codec<Map<BusSignalRef, List<NetReference>>> INPUT_CODEC = Codecs.codecForMap(
            BusSignalRef.CODEC, Codec.list(NetReference.CODEC)
    );
    private static final Codec<Map<NetReference, Double>> CONSTANCE_CODEC = Codecs.codecForMap(
            NetReference.CODEC, Codec.DOUBLE
    );
    private final Circuit circuit;
    private final Map<NetReference, List<BusSignalRef>> outputConnections;
    private final Map<BusSignalRef, List<NetReference>> inputConnections;
    private final Map<NetReference, Double> constantInputs;
    private BusState outputValues = BusState.EMPTY;

    public BusConnectedCircuit(
            Circuit circuit,
            Map<NetReference, List<BusSignalRef>> outputConnections,
            Map<BusSignalRef, List<NetReference>> inputConnections,
            Map<NetReference, Double> constantInputs
    ) {
        this.circuit = circuit;
        this.outputConnections = outputConnections;
        this.inputConnections = inputConnections;
        this.constantInputs = constantInputs;
        Preconditions.checkArgument(
                inputConnections.values().stream()
                        .flatMap(List::stream)
                        .noneMatch(constantInputs::containsKey)
        );
        constantInputs.forEach(circuit::updateInputValue);
    }

    public BusConnectedCircuit(CompoundNBT nbt) {
        this(
                Circuit.fromNBT(nbt.getCompound("circuit")),
                Codecs.readOrThrow(OUTPUT_CODEC, nbt.get("outputs")),
                Codecs.readOrThrow(INPUT_CODEC, nbt.get("inputs")),
                Codecs.readOrThrow(CONSTANCE_CODEC, nbt.get("constants"))
        );
    }

    public void updateInputs(BusState bus) {
        for (Map.Entry<BusSignalRef, List<NetReference>> netPair : inputConnections.entrySet()) {
            for (NetReference circuitNet : netPair.getValue()) {
                circuit.updateInputValue(
                        circuitNet, bus.getSignal(netPair.getKey()) / (double) BusLine.MAX_VALID_VALUE
                );
            }
        }
    }

    public boolean tick() {
        circuit.tick();
        boolean changed = false;
        for (Map.Entry<NetReference, List<BusSignalRef>> output : outputConnections.entrySet()) {
            final int newValue = (int) MathHelper.clamp(
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

    public CompoundNBT toNBT() {
        CompoundNBT result = new CompoundNBT();
        result.put("circuit", circuit.toNBT());
        result.put("inputs", Codecs.encode(INPUT_CODEC, inputConnections));
        result.put("outputs", Codecs.encode(OUTPUT_CODEC, outputConnections));
        result.put("constants", Codecs.encode(CONSTANCE_CODEC, constantInputs));
        return result;
    }

    public BusState getOutputState() {
        return outputValues;
    }

    private int getTotalCost(ToDoubleFunction<CellCost> individualCost) {
        return MathHelper.ceil(getCircuit()
                .getCellTypes()
                .map(LeafcellType::getCost)
                .mapToDouble(individualCost)
                .sum());
    }

    public int getNumTubes() {
        return getTotalCost(CellCost::getNumTubes);
    }

    public int getWireLength() {
        return getTotalCost(CellCost::getWireLength);
    }

    public int getSolderAmount() {
        return getTotalCost(CellCost::getSolderAmount);
    }
}
