package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.CEIICBlockEntity;
import malte0811.controlengineering.blockentity.base.INeighborChangeListener;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class BusInterfaceBlock extends CEIICBlockEntity implements IBusConnector, INeighborChangeListener {
    private final Map<Direction, Pair<WeakReference<IBusInterface>, Runnable>> clearers = new EnumMap<>(Direction.class);

    public BusInterfaceBlock(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {
        BusState state = getBusHandler(updatedPoint).getState();
        getConnectedBE().forEach(iBusInterface -> iBusInterface.onBusUpdated(state));
    }

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        return getConnectedBE()
                .stream()
                .map(IBusInterface::getEmittedState)
                .reduce(BusState.EMPTY, BusState::merge);
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

    private Collection<IBusInterface> getConnectedBE() {
        Collection<IBusInterface> ret = new ArrayList<>();
        Direction facing = getFacing();
        BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(facing));
        if (neighbor instanceof IBusInterface) {
            IBusInterface i = (IBusInterface) neighbor;
            if (i.canConnect(facing.getOpposite())) {
                Pair<WeakReference<IBusInterface>, Runnable> existing = clearers.get(facing);
                if (existing == null || existing.getFirst().get() != i) {
                    Pair<Clearable<Runnable>, Runnable> newClearer = Clearable.create(() -> getBusHandler(new ConnectionPoint(
                            worldPosition, 0
                    )).requestUpdate());
                    i.addMarkDirtyCallback(newClearer.getFirst());
                    clearers.put(facing, Pair.of(new WeakReference<>(i), newClearer.getSecond()));
                }
                ret.add(i);
            }
        }
        return ret;
    }

    private Direction getFacing() {
        return getBlockState().getValue(malte0811.controlengineering.blocks.bus.BusInterfaceBlock.FACING);
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(LocalBusHandler.NAME);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        clearers.values().stream()
                .map(Pair::getSecond)
                .forEach(Runnable::run);
    }

    @Override
    public void onNeighborChanged(BlockPos neighbor) {
        //TODO more intelligent approach?
        getBusHandler(new ConnectionPoint(worldPosition, 0)).requestUpdate();
    }
}
