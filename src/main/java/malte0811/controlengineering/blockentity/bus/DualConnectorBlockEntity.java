package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blockentity.CEIICBlockEntity;
import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class DualConnectorBlockEntity extends CEIICBlockEntity {
    protected static final int MIN_ID = 0;
    protected static final int MAX_ID = 1;
    protected final ConnectionPoint minPoint;
    protected final ConnectionPoint maxPoint;

    public DualConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        minPoint = new ConnectionPoint(worldPosition, MIN_ID);
        maxPoint = new ConnectionPoint(worldPosition, MAX_ID);
    }

    @Override
    public final Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        final double offset;
        if (here.index() == MAX_ID) {
            offset = .25;
        } else {
            offset = -.25;
        }
        return new Vec3(0.5, 7 / 16., 0.5).add(
                Vec3.atLowerCornerOf(getBlockState().getValue(LineAccessBlock.FACING).getNormal())
                        .scale(offset)
        );
    }

    @Nonnull
    @Override
    public final ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset) {
        Direction facing = getBlockState().getValue(LineAccessBlock.FACING);
        Vec3i normal = facing.getNormal();
        if (normal.getX() * (info.hitX - .5) + normal.getY() * (info.hitY - .5) + normal.getZ() * (info.hitZ - .5) < 0) {
            return minPoint;
        } else {
            return maxPoint;
        }
    }

    @Override
    public final Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableList.of(maxPoint, minPoint);
    }

    protected final ConnectionPoint getOtherPoint(ConnectionPoint cp) {
        return cp.equals(maxPoint) ? minPoint : maxPoint;
    }
}
