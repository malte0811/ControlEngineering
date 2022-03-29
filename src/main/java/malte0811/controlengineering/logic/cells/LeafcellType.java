package malte0811.controlengineering.logic.cells;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public abstract class LeafcellType<State> extends TypedRegistryEntry<State, LeafcellInstance<State>> {
    public static final TypedRegistry<LeafcellType<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends LeafcellType<?>> T register(ResourceLocation name, T type) {
        return REGISTRY.register(name, type);
    }

    public static final String DEFAULT_OUT_NAME = "out";
    public static final String DEFAULT_IN_NAME = "in";

    private final Map<String, Pin> inputPins;
    private final Map<String, Pin> outputPins;
    private final CellCost cost;

    protected LeafcellType(
            Map<String, Pin> inputPins,
            Map<String, Pin> outputPins,
            State initialState,
            MyCodec<State> stateCodec,
            CellCost cost
    ) {
        super(initialState, stateCodec);
        Preconditions.checkArgument(inputPins.values().stream().noneMatch(p -> p.direction().isOutput()));
        Preconditions.checkArgument(outputPins.values().stream().allMatch(p -> p.direction().isOutput()));
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.cost = cost;
    }

    @Override
    public LeafcellInstance<State> newInstance(State state) {
        return new LeafcellInstance<>(this, state);
    }

    public abstract State nextState(Object2DoubleMap<String> inputSignals, State currentState);

    public abstract Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, State oldState);

    public Map<String, Pin> getInputPins() {
        return inputPins;
    }

    public Map<String, Pin> getOutputPins() {
        return outputPins;
    }

    public CellCost getCost() {
        return cost;
    }

    protected static boolean bool(double value) {
        return value > 0.5;
    }

    protected static double debool(boolean value) {
        return value ? 1 : 0;
    }
}
