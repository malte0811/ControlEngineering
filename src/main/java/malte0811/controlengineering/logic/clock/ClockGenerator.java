package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.typereg.TypedInstance;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;

public abstract class ClockGenerator<State> extends TypedRegistryEntry<State, ClockGenerator.ClockInstance<State>> {
    protected ClockGenerator(State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    public abstract boolean shouldTick(State oldState, boolean triggerSignal);

    public abstract State nextState(State oldState, boolean triggerSignal);

    @Override
    public ClockInstance<State> newInstance(State state) {
        return new ClockInstance<>(this, state);
    }

    public boolean isActiveClock() {
        return true;
    }

    public static class ClockInstance<State> extends TypedInstance<State, ClockGenerator<State>> {
        public static final Codec<ClockInstance<?>> CODEC = TypedInstance.makeCodec(ClockTypes.REGISTRY);

        public ClockInstance(ClockGenerator<State> stateClockGenerator, State currentState) {
            super(stateClockGenerator, currentState);
        }

        public boolean tick(boolean triggerIn) {
            State nextState = getType().nextState(getCurrentState(), triggerIn);
            boolean result = getType().shouldTick(getCurrentState(), triggerIn);
            this.currentState = nextState;
            return result;
        }
    }
}
