package malte0811.controlengineering.logic.cells;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;

public final class LeafcellInstance<State, Config> extends TypedInstance<
        Pair<State, Config>, LeafcellType<State, Config>
        > {
    public static final MyCodec<LeafcellInstance<?, ?>> CODEC = makeCodec(LeafcellType.REGISTRY);

    public LeafcellInstance(LeafcellType<State, Config> type, State currentState, Config currentConfig) {
        this(type, Pair.of(currentState, currentConfig));
    }

    public LeafcellInstance(LeafcellType<State, Config> type, Pair<State, Config> state) {
        super(type, state);
    }

    public CircuitSignals tick(CircuitSignals inputValues) {
        final var lastState = currentState.getFirst();
        final var config = currentState.getSecond();
        final var newState = getType().nextState(inputValues, currentState.getFirst(), config);
        currentState = Pair.of(newState, config);
        return getType().getOutputSignals(inputValues, lastState, config);
    }

    public CircuitSignals getCurrentOutput(CircuitSignals inputs) {
        return getType().getOutputSignals(inputs, currentState.getFirst(), currentState.getSecond());
    }
}
