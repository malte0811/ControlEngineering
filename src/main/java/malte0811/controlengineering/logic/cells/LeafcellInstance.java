package malte0811.controlengineering.logic.cells;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.util.typereg.TypedInstance;

public final class LeafcellInstance<State> extends TypedInstance<State, LeafcellType<State>> {
    public static final Codec<LeafcellInstance<?>> CODEC = makeCodec(LeafcellType.REGISTRY);

    public LeafcellInstance(LeafcellType<State> type, State currentState) {
        super(type, currentState);
    }

    public Object2DoubleMap<String> tick(Object2DoubleMap<String> inputValues) {
        final State lastState = currentState;
        currentState = getType().nextState(inputValues, currentState);
        return getType().getOutputSignals(inputValues, lastState);
    }

    public Object2DoubleMap<String> getCurrentOutput(Object2DoubleMap<String> inputs) {
        return getType().getOutputSignals(inputs, currentState);
    }
}
