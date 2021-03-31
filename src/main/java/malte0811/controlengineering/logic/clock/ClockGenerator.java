package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.typereg.TypedInstance;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.nbt.CompoundNBT;

public abstract class ClockGenerator<State> extends TypedRegistryEntry<State> {
    protected ClockGenerator(State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    public abstract boolean shouldTick(State oldState, boolean triggerSignal);

    public abstract State nextState(State oldState, boolean triggerSignal);

    @Override
    public ClockInstance<State> newInstance() {
        return new ClockInstance<>(this, getInitialState());
    }

    public boolean isActiveClock() {
        return true;
    }

    public static class ClockInstance<State> extends TypedInstance<State, ClockGenerator<State>> {
        public ClockInstance(ClockGenerator<State> stateClockGenerator, State currentState) {
            super(stateClockGenerator, currentState);
        }

        public static ClockInstance<?> fromNBT(CompoundNBT nbt) {
            return fromNBT(nbt, ClockTypes.REGISTRY, ClockInstance::makeUnchecked);
        }

        private static <T> ClockInstance<T> makeUnchecked(ClockGenerator<T> type, Object state) {
            return new ClockInstance<>(type, (T) state);
        }

        public boolean tick(boolean triggerIn) {
            State nextState = getType().nextState(getCurrentState(), triggerIn);
            boolean result = getType().shouldTick(getCurrentState(), triggerIn);
            this.currentState = nextState;
            return result;
        }
    }
}
