package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.util.mycodec.MyCodecs;

public class EdgeClock extends ClockGenerator<Boolean> {
    protected EdgeClock() {
        super(false, MyCodecs.BOOL);
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
