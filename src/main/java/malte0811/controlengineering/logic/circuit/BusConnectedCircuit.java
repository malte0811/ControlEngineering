package malte0811.controlengineering.logic.circuit;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.BusWireTypes;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;

public class BusConnectedCircuit {
    //TODO make available in UI
    public static final NetReference ZERO = new NetReference("zero");
    public static final NetReference ONE = new NetReference("one");

    private static final Codec<Map<NetReference, List<BusSignalRef>>> OUTPUT_CODEC = Codecs.codecForMap(
            NetReference.CODEC, Codec.list(BusSignalRef.CODEC)
    );
    private static final Codec<Map<BusSignalRef, List<NetReference>>> INPUT_CODEC = Codecs.codecForMap(
            BusSignalRef.CODEC, Codec.list(NetReference.CODEC)
    );
    private final Circuit circuit;
    private final Map<NetReference, List<BusSignalRef>> outputConnections;
    private final Map<BusSignalRef, List<NetReference>> inputConnections;
    private BusState outputValues = new BusState(BusWireTypes.MAX_BUS_WIDTH);

    public BusConnectedCircuit(
            Circuit circuit,
            Map<NetReference, List<BusSignalRef>> outputConnections,
            Map<BusSignalRef, List<NetReference>> inputConnections
    ) {
        this.circuit = circuit;
        this.outputConnections = outputConnections;
        this.inputConnections = inputConnections;
        circuit.updateInputValue(ONE, 1);
        circuit.updateInputValue(ZERO, 1);
    }

    public BusConnectedCircuit(CompoundNBT nbt) {
        this(
                new Circuit(nbt.getCompound("circuit")),
                Codecs.readOrThrow(OUTPUT_CODEC, nbt.get("outputs")),
                Codecs.readOrThrow(INPUT_CODEC, nbt.get("inputs"))
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
        return result;
    }
}
