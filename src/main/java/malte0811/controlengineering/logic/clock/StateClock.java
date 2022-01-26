package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.util.Unit;

public class StateClock extends ClockGenerator<Unit> {
    protected StateClock() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE));
    }

    @Override
    public boolean shouldTick(Unit oldState, boolean triggerSignal) {
        return triggerSignal;
    }

    @Override
    public Unit nextState(Unit oldState, boolean triggerSignal) {
        return Unit.INSTANCE;
    }
}
