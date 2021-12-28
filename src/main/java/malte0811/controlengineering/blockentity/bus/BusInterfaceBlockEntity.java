package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.CEIICBlockEntity;
import malte0811.controlengineering.blockentity.base.INeighborChangeListener;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.bus.IBusInterface;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.util.Clearable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collection;

public class BusInterfaceBlockEntity extends CEIICBlockEntity implements IBusConnector, INeighborChangeListener {
    private Pair<WeakReference<IBusInterface>, Runnable> clearer = null;

    public BusInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {
        var connected = getConnectedBE();
        if (connected != null) {
            var handler = getBusHandler(updatedPoint);
            connected.onBusUpdated(handler.getState());
        }
    }

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        var connected = getConnectedBE();
        if (connected != null) {
            return connected.getEmittedState();
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return globalNet.getLocalNet(worldPosition);
    }

    @Override
    public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type) {
        return new Vec3(0.5, 0.5, 0.5)
                .add(Vec3.atLowerCornerOf(getFacing().getNormal()).scale(1. / 16));
    }

    @Nullable
    private IBusInterface getConnectedBE() {
        Direction facing = getFacing();
        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(facing));
        if (neighbor instanceof IBusInterface busInterface && busInterface.canConnect(facing.getOpposite())) {
            if (clearer == null || clearer.getFirst().get() != busInterface) {
                Pair<Clearable<Runnable>, Runnable> newClearer = Clearable.create(
                        () -> getBusHandler(new ConnectionPoint(worldPosition, 0)).requestUpdate()
                );
                busInterface.addMarkDirtyCallback(newClearer.getFirst());
                clearer = Pair.of(new WeakReference<>(busInterface), newClearer.getSecond());
            }
            return busInterface;
        }
        return null;
    }

    private Direction getFacing() {
        return getBlockState().getValue(BusInterfaceBlock.FACING);
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(LocalBusHandler.NAME);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (clearer != null) {
            clearer.getSecond().run();
        }
    }

    @Override
    public void onNeighborChanged(BlockPos neighbor) {
        //TODO more intelligent approach?
        getBusHandler(new ConnectionPoint(worldPosition, 0)).requestUpdate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide()) {
            ApiUtils.addFutureServerTask(level, this::getConnectedBE, true);
        }
    }
}
