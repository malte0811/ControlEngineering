package malte0811.controlengineering.logic.cells;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.util.typereg.TypedInstance;

public final class LeafcellInstance<State> extends TypedInstance<State, LeafcellType<State>> {
    public static final Codec<LeafcellInstance<?>> CODEC = makeCodec(
            LeafcellType.REGISTRY, LeafcellInstance::makeUnchecked
    );

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        super(type, currentState);
    }

    public DoubleList tick(DoubleList inputValues) {
        final State lastState = currentState;
        currentState = getType().nextState(inputValues, currentState);
        return getType().getOutputSignals(inputValues, lastState);
    }

    public DoubleList getCurrentOutput(DoubleList inputs) {
        return getType().getOutputSignals(inputs, currentState);
    }

    private static <T> LeafcellInstance<T> makeUnchecked(LeafcellType<T> type, Object state) {
        return new LeafcellInstance<>(type, (T) state);
    }
}
