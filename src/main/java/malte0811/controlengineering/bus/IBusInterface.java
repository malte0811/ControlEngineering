package malte0811.controlengineering.bus;

import malte0811.controlengineering.util.Clearable;
import net.minecraft.util.Direction;

public interface IBusInterface {
    void onBusUpdated(BusState newState);

    BusState getEmittedState();

    boolean canConnect(Direction fromSide);

    void addMarkDirtyCallback(Clearable<Runnable> markDirty);
}
