package malte0811.controlengineering.logic.clock;

import com.mojang.serialization.Codec;
import net.minecraft.util.Unit;

public class NoneClock extends ClockGenerator<Unit> {
    public NoneClock() {
        super(Unit.INSTANCE, Codec.unit(Unit.INSTANCE));
    }

    @Override
    public boolean shouldTick(Unit oldState, boolean triggerSignal) {
        return false;
    }

    @Override
    public Unit nextState(Unit oldState, boolean triggerSignal) {
        return oldState;
    }
}
