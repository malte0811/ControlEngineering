package malte0811.controlengineering.logic.cells;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public abstract class LeafcellType<State> extends TypedRegistryEntry<State> {
    static final TypedRegistry<LeafcellType<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends LeafcellType<?>> T register(ResourceLocation name, T type) {
        return REGISTRY.register(name, type);
    }

    private final List<Pin> inputPins;
    private final List<Pin> outputPins;

    protected LeafcellType(List<Pin> inputPins, List<Pin> outputPins, State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
        this.inputPins = inputPins;
        this.outputPins = outputPins;
    }

    public LeafcellInstance<State> newInstance() {
        return new LeafcellInstance<>(this, getInitialState());
    }

    public abstract State nextState(DoubleList inputSignals, State currentState);

    public abstract DoubleList getOutputSignals(DoubleList inputSignals, State currentState);

    public List<Pin> getInputPins() {
        return inputPins;
    }

    public List<Pin> getOutputPins() {
        return outputPins;
    }

    protected static boolean bool(double value) {
        return value == 1;
    }
}
