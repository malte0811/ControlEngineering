package malte0811.controlengineering.logic.cells;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public abstract class LeafcellType<State, Config> extends TypedRegistryEntry<
        Pair<State, Config>, LeafcellInstance<State, Config>
        > {
    public static final TypedRegistry<LeafcellType<?, ?>> REGISTRY = new TypedRegistry<>();
    private final MyCodec<Config> configCodec;

    public static <T extends LeafcellType<?, ?>> T register(ResourceLocation name, T type) {
        return REGISTRY.register(name, type);
    }

    public static final String DEFAULT_OUT_NAME = "out";
    public static final String DEFAULT_IN_NAME = "in";

    private final Map<String, Pin> inputPins;
    private final Map<String, Pin> outputPins;
    private final CellCost cost;

    protected LeafcellType(
            Map<String, Pin> inputPins, Map<String, Pin> outputPins,
            State initialState, MyCodec<State> stateCodec,
            Config initialConfig, MyCodec<Config> configCodec,
            CellCost cost
    ) {
        this(
                inputPins, outputPins,
                initialState, initialConfig,
                MyCodecs.pair(stateCodec, configCodec), configCodec,
                cost
        );
    }

    protected LeafcellType(
            Map<String, Pin> inputPins, Map<String, Pin> outputPins,
            State initialState, Config initialConfig, MyCodec<Pair<State, Config>> codec,
            MyCodec<Config> configCodec, CellCost cost
    ) {
        super(Pair.of(initialState, initialConfig), codec);
        Preconditions.checkArgument(inputPins.values().stream().noneMatch(p -> p.direction().isOutput()));
        Preconditions.checkArgument(outputPins.values().stream().allMatch(p -> p.direction().isOutput()));
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.cost = cost;
        this.configCodec = configCodec;
    }

    public LeafcellInstance<State, Config> newInstanceFromConfig(Config config) {
        return newInstance(Pair.of(getInitialState().getFirst(), config));
    }

    @Override
    public LeafcellInstance<State, Config> newInstance(Pair<State, Config> state) {
        return new LeafcellInstance<>(this, state);
    }

    public State nextState(CircuitSignals inputSignals, State currentState, Config config) {
        return currentState;
    }

    public abstract CircuitSignals getOutputSignals(CircuitSignals inputSignals, State oldState, Config config);

    public Map<String, Pin> getInputPins() {
        return inputPins;
    }

    public Map<String, Pin> getOutputPins() {
        return outputPins;
    }

    public CellCost getCost() {
        return cost;
    }

    public MyCodec<Config> getConfigCodec() {
        return configCodec;
    }
}
