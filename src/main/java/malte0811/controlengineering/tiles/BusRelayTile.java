package malte0811.controlengineering.tiles;

import blusunrize.immersiveengineering.api.wires.*;
import com.google.common.base.Preconditions;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.BusWireTypes;
import malte0811.controlengineering.bus.IBusConnector;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BusRelayTile extends ImmersiveConnectableTileEntity implements IBusConnector
{
    public BusRelayTile() {
        super(CETileEntities.BUS_RELAY.get());
    }

    @Override
    public int getMinBusWidthForConfig(ConnectionPoint cp) {
        return BusWireTypes.MIN_BUS_WIDTH;
    }

    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {}

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        return new BusState(getCurrentBusWidth(checkedPoint).orElse(getMinBusWidthForConfig(checkedPoint)));
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    //TODO change once a model exists
    @Override
    public Vector3d getConnectionOffset(
            @Nonnull Connection connection, ConnectionPoint connectionPoint
    ) {
        return new Vector3d(0.5, 0.5, 0.5);
    }
}
