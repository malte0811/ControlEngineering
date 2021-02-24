package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;

public class EdgeClock extends ClockGenerator<Boolean> {
    protected EdgeClock() {
        super(false, Codec.BOOL);
    }

    @Override
    public boolean shouldTick(Boolean oldState, boolean triggerSignal) {
        return triggerSignal && !oldState;
    }

    @Override
    public Boolean nextState(Boolean oldState, boolean triggerSignal) {
        return triggerSignal;
    }
}
