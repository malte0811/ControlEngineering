package malte0811.controlengineering.blockentity;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.impl.ImmersiveConnectableBlockEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

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

    protected LocalWireNetwork getLocalNet(ConnectionPoint cp) {
        Preconditions.checkArgument(cp.position().equals(worldPosition));
        return super.getLocalNet(cp.index());
    }

    protected Collection<Connection> getConnections(ConnectionPoint cp) {
        return getLocalNet(cp).getConnections(cp);
    }

    protected int countRealWiresAt(ConnectionPoint cp) {
        return (int) getConnections(cp).stream()
                .filter(Predicate.not(Connection::isInternal))
                .count();
    }
}
