package malte0811.controlengineering.tiles.bus;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.tiles.CEIICTileEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;

public class BusRelayTile extends CEIICTileEntity implements IBusConnector {
    public BusRelayTile(BlockEntityType<?> tileEntityTypeIn) {
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
    public Vec3 getConnectionOffset(@Nonnull Connection connection, ConnectionPoint connectionPoint) {
        return new Vec3(0.5, 0.5, 0.5)
                .add(Vec3.atLowerCornerOf(getFacing().getNormal()).scale(1.5 / 16));
    }

    private Direction getFacing() {
        return getBlockState().getValue(BusInterfaceBlock.FACING);
    }
}
