package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.util.Unit;

public class NoneClock extends ClockGenerator<Unit> {
    public NoneClock() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE));
    }

    @Override
    public boolean shouldTick(Unit oldState, boolean triggerSignal) {
        return false;
    }

    @Override
    public Unit nextState(Unit oldState, boolean triggerSignal) {
        return oldState;
    }

    @Override
    public boolean isActiveClock() {
        return false;
    }
}
