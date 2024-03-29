package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import net.minecraft.core.Vec3i;

public interface IBusConnector extends IImmersiveConnectable {
    void onBusUpdated(ConnectionPoint updatedPoint);

    BusState getEmittedState(ConnectionPoint checkedPoint);

    LocalWireNetwork getLocalNet(int cpIndex);

    @Override
    default boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vec3i offset) {
        return wireType == BusWireType.INSTANCE || !isBusPoint(connectionPoint);
    }

    default LocalBusHandler getBusHandler(ConnectionPoint cp) {
        return getLocalNet(cp.index()).getHandler(LocalBusHandler.NAME, LocalBusHandler.class);
    }

    default boolean isBusPoint(ConnectionPoint cp) {
        return true;
    }
}
