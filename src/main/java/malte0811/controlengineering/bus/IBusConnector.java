package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.base.Preconditions;
import malte0811.controlengineering.bus.BusWireTypes;
import net.minecraft.util.math.vector.Vector3i;
import org.apache.http.pool.ConnPool;

import java.util.Optional;

public interface IBusConnector extends IImmersiveConnectable {
    int getMinBusWidthForConfig(ConnectionPoint cp);

    void onBusUpdated(ConnectionPoint updatedPoint);

    BusState getEmittedState(ConnectionPoint checkedPoint);

    LocalWireNetwork getLocalNet(int cpIndex);

    @Override
    default boolean canConnectCable(
            WireType wireType, ConnectionPoint connectionPoint, Vector3i offset
    ) {
        if (!(wireType instanceof BusWireTypes.BusWireType))
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
        return getLocalNet(cp.getIndex()).getConnections(cp).stream().findAny().map(c -> {
            Preconditions.checkArgument(c.type instanceof BusWireTypes.BusWireType, "Unexpected wire type: %s", c.type);
            return ((BusWireTypes.BusWireType) c.type).getWidth();
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
}
