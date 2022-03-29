package malte0811.controlengineering.logic.clock;

import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.util.Unit;

public class FreeClock extends ClockGenerator<Unit> {
    protected FreeClock() {
        super(Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE));
    }

    @Override
    public boolean shouldTick(Unit oldState, boolean triggerSignal) {
        return true;
    }

    @Override
    public Unit nextState(Unit oldState, boolean triggerSignal) {
        return Unit.INSTANCE;
    }
}
