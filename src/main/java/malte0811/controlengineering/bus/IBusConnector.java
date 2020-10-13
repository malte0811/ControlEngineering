package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.util.math.vector.Vector3i;

import java.util.Optional;

public interface IBusConnector extends IImmersiveConnectable {
    int getMinBusWidthForConfig(ConnectionPoint cp);

    void onBusUpdated(ConnectionPoint updatedPoint);

    BusState getEmittedState(ConnectionPoint checkedPoint);

    LocalWireNetwork getLocalNet(int cpIndex);

    @Override
    default boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vector3i offset) {
        if (!(wireType instanceof BusWireTypes.BusWireType) || !isBusPoint(connectionPoint))
            return false;
        else {
            int width = ((BusWireTypes.BusWireType) wireType).getWidth();
            Optional<Integer> activeWidth = getCurrentBusWidth(connectionPoint);
            if (activeWidth.isPresent())
                return activeWidth.get() == width;
            else
                return width >= getMinBusWidthForConfig(connectionPoint);
        }
    }

    default Optional<Integer> getCurrentBusWidth(ConnectionPoint cp) {
        if (!isBusPoint(cp)) {
            return Optional.empty();
        }
        return getLocalNet(cp.getIndex()).getConnections(cp).stream().findAny().map(c -> {
            if (c.type instanceof BusWireTypes.BusWireType)
                return ((BusWireTypes.BusWireType) c.type).getWidth();
            else {
                ControlEngineering.LOGGER.warn("Expected bus wire type, got {} ({})", c.type, c.type.getUniqueName());
                return 0;
            }
        });
    }

    default int getActualBusWidth(ConnectionPoint checkedPoint) {
        return getCurrentBusWidth(checkedPoint)
                .orElse(getMinBusWidthForConfig(checkedPoint));
    }

    default LocalBusHandler getBusHandler(ConnectionPoint cp) {
        return getLocalNet(cp.getIndex())
                .getHandler(LocalBusHandler.NAME, LocalBusHandler.class);
    }

    default boolean isBusPoint(ConnectionPoint cp) {
        return true;
    }
}
