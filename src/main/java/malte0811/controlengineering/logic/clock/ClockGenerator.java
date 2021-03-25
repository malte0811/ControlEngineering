package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.typereg.TypedInstance;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public abstract class ClockGenerator<State> extends TypedRegistryEntry<State> {
    private static final TypedRegistry<ClockGenerator<?>> REGISTRY = new TypedRegistry<>();

    public static <T extends ClockGenerator<?>> T register(ResourceLocation name, T generator) {
        return REGISTRY.register(name, generator);
    }

    protected ClockGenerator(State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    public abstract boolean shouldTick(State oldState, boolean triggerSignal);

    public abstract State nextState(State oldState, boolean triggerSignal);

    @Override
    public ClockInstance<State> newInstance() {
        return new ClockInstance<>(this, getInitialState());
    }

    public static class ClockInstance<State> extends TypedInstance<State, ClockGenerator<State>> {
        public ClockInstance(ClockGenerator<State> stateClockGenerator, State currentState) {
            super(stateClockGenerator, currentState);
        }

        public static ClockInstance<?> fromNBT(CompoundNBT nbt) {
            return fromNBT(nbt, REGISTRY, ClockInstance::makeUnchecked);
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
