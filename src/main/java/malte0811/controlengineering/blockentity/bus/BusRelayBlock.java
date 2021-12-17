package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import malte0811.controlengineering.blockentity.CEIICBlockEntity;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BusRelayBlock extends CEIICBlockEntity implements IBusConnector {
    public BusRelayBlock(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        return new Vec3(0.5, 0.5, 0.5)
                .add(Vec3.atLowerCornerOf(getFacing().getNormal()).scale(1.5 / 16));
    }

    private Direction getFacing() {
        return getBlockState().getValue(BusInterfaceBlock.FACING);
    }
}
