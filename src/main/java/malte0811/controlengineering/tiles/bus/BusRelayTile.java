package malte0811.controlengineering.tiles.bus;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.tiles.CEIICTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public class BusRelayTile extends CEIICTileEntity implements IBusConnector {
    public BusRelayTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {}

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        return BusState.EMPTY;
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    @Override
    public Vector3d getConnectionOffset(@Nonnull Connection connection, ConnectionPoint connectionPoint) {
        return new Vector3d(0.5, 0.5, 0.5)
                .add(Vector3d.copy(getFacing().getDirectionVec()).scale(1.5 / 16));
    }

    private Direction getFacing() {
        return getBlockState().get(BusInterfaceBlock.FACING);
    }
}
