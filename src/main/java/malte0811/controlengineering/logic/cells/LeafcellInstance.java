package malte0811.controlengineering.logic.cells;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedInstance;

public final class LeafcellInstance<State> extends TypedInstance<State, LeafcellType<State>> {
    public static final MyCodec<LeafcellInstance<?>> CODEC = makeCodec(LeafcellType.REGISTRY);

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        super(type, currentState);
    }

    public CircuitSignals tick(CircuitSignals inputValues) {
        final State lastState = currentState;
        currentState = getType().nextState(inputValues, currentState);
        return getType().getOutputSignals(inputValues, lastState);
    }

    public CircuitSignals getCurrentOutput(CircuitSignals inputs) {
        return getType().getOutputSignals(inputs, currentState);
    }
}
