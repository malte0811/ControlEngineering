package malte0811.controlengineering.logic.cells;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public abstract class LeafcellType<State> extends TypedRegistryEntry<State> {
    static final TypedRegistry<LeafcellType<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends LeafcellType<?>> T register(ResourceLocation name, T type) {
        return REGISTRY.register(name, type);
    }

    public static final String DEFAULT_OUT_NAME = "out";
    public static final String DEFAULT_IN_NAME = "in";

    private final Map<String, Pin> inputPins;
    private final Map<String, Pin> outputPins;
    private final double numTubes;

    protected LeafcellType(
            Map<String, Pin> inputPins,
            Map<String, Pin> outputPins,
            State initialState,
            Codec<State> stateCodec,
            int numTubes
    ) {
        super(initialState, stateCodec);
        Preconditions.checkArgument(inputPins.values().stream().noneMatch(p -> p.getDirection().isOutput()));
        Preconditions.checkArgument(outputPins.values().stream().allMatch(p -> p.getDirection().isOutput()));
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.numTubes = numTubes;
    }

    @Override
    public LeafcellInstance<State> newInstance() {
        return new LeafcellInstance<>(this, getInitialState());
    }

    public abstract State nextState(Object2DoubleMap<String> inputSignals, State currentState);

    public abstract Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, State oldState);

    public Map<String, Pin> getInputPins() {
        return inputPins;
    }

    public Map<String, Pin> getOutputPins() {
        return outputPins;
    }

    public double getNumTubes() {
        return numTubes;
    }

    protected static boolean bool(double value) {
        return value > 0.5;
    }
}
