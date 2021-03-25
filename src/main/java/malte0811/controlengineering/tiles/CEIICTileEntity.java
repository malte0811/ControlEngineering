package malte0811.controlengineering.tiles;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.impl.ImmersiveConnectableTileEntity;
import com.google.common.collect.ImmutableSet;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.Collection;

public abstract class CEIICTileEntity extends ImmersiveConnectableTileEntity {

    public CEIICTileEntity(TileEntityType<?> tileEntityTypeIn) {
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
        return pos;
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
    public ConnectionPoint getTargetedPoint(TargetingInfo targetingInfo, Vector3i vector3i) {
        return new ConnectionPoint(pos, 0);
    }

    @Override
    public void removeCable(@Nullable Connection connection, ConnectionPoint connectionPoint) {}

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableSet.of(new ConnectionPoint(pos, 0));
    }
}
