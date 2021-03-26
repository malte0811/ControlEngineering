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
    private final double numTubes;

    protected LeafcellType(
            List<Pin> inputPins,
            List<Pin> outputPins,
            State initialState,
            Codec<State> stateCodec,
            double numTubes
    ) {
        super(initialState, stateCodec);
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.numTubes = numTubes;
    }

    @Override
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

    public double getNumTubes() {
        return numTubes;
    }

    protected static boolean bool(double value) {
        return value > 0.5;
    }
}
