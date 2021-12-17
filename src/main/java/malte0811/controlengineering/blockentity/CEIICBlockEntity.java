package malte0811.controlengineering.blockentity;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.impl.ImmersiveConnectableBlockEntity;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class CEIICBlockEntity extends ImmersiveConnectableBlockEntity {

    public CEIICBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public BlockPos getConnectionMaster(
            @Nullable WireType wireType, TargetingInfo targetingInfo
    ) {
        return worldPosition;
    }

    @Override
    public void connectCable(
            WireType wireType,
            ConnectionPoint connectionPoint,
            IImmersiveConnectable iImmersiveConnectable,
            ConnectionPoint connectionPoint1
    ) {}

    @Nullable
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo targetingInfo, Vec3i vector3i) {
        return new ConnectionPoint(worldPosition, 0);
    }

    @Override
    public void removeCable(@Nullable Connection connection, ConnectionPoint connectionPoint) {}

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableSet.of(new ConnectionPoint(worldPosition, 0));
    }

    @Override
    public final BlockPos getPosition() {
        return getBlockPos();
    }

    //TODO Workaround for Forge#7926
    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        ApiUtils.addFutureServerTask(level, this::onLoad);
    }
}
