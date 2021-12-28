package malte0811.controlengineering.bus;

import malte0811.controlengineering.util.Clearable;
import net.minecraft.core.Direction;

public interface IBusInterface {
    void onBusUpdated(BusState totalState, BusState otherState);

    BusState getEmittedState();

    boolean canConnect(Direction fromSide);

    void addMarkDirtyCallback(Clearable<Runnable> markDirty);
}
