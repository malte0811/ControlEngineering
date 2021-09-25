package malte0811.controlengineering.tiles;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.impl.ImmersiveConnectableTileEntity;
import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntityType;
import java.util.Collection;

public abstract class CEIICTileEntity extends ImmersiveConnectableTileEntity {

    public CEIICTileEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
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
}
