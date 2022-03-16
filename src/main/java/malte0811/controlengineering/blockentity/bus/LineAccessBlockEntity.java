package malte0811.controlengineering.blockentity.bus;

import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.bus.LocalBusHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class LineAccessBlockEntity extends DualConnectorBlockEntity implements IBusConnector, IRedstoneConnector {
    private static final int REDSTONE_ID = MIN_ID;
    private static final int BUS_ID = MAX_ID;

    public int selectedLine;
    private BusLine lastLineToRS = new BusLine();

    public LineAccessBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        selectedLine = nbt.getInt("selectedLine");
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("selectedLine", selectedLine);
    }

    /*BUS*/
    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {
        RedstoneNetworkHandler rsHandler = getRSNet();
        BusLine lineToRS = getBusNet().getStateWithout(getBusPoint(), this).getLine(selectedLine);
        if (rsHandler != null && !this.lastLineToRS.equals(lineToRS)) {
            rsHandler.updateValues();
            this.lastLineToRS = lineToRS;
        }
    }

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        BusState ret = BusState.EMPTY;
        var rs = getRSNet();
        if (rs != null) {
            ret = ret.withLine(selectedLine, BusLine.fromRSState(rs.getValuesExcluding(getRedstonePoint())));
        }
        return ret;
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vec3i offset) {
        if (countRealWiresAt(connectionPoint) > 0) {
            return false;
        }
        if (connectionPoint.index() == BUS_ID) {
            return IBusConnector.super.canConnectCable(wireType, connectionPoint, offset);
        } else {
            return wireType.getCategory().equals(WireType.REDSTONE_CATEGORY);
        }
    }

    /*REDSTONE*/
    @Override
    public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        //TODO more intelligent behavior?
        if (getRedstonePoint().equals(cp)) {
            getBusNet().requestUpdate();
        }
    }

    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        if (getRedstonePoint().equals(cp)) {
            BusLine line = getBusNet().getStateWithout(getBusPoint(), this).getLine(selectedLine);
            for (int i = 0; i < signals.length; ++i) {
                signals[i] = (byte) Math.max(line.getRSValue(i), signals[i]);
            }
        }
    }

    @Nullable
    private RedstoneNetworkHandler getRSNet() {
        return getLocalNet(REDSTONE_ID)
                .getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
    }

    private LocalBusHandler getBusNet() {
        return Objects.requireNonNull(getBusHandler(getBusPoint()));
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(LocalBusHandler.NAME, RedstoneNetworkHandler.ID);
    }

    @Override
    public boolean isBusPoint(ConnectionPoint cp) {
        return getBusPoint().equals(cp);
    }

    private ConnectionPoint getRedstonePoint() {
        return minPoint;
    }

    private ConnectionPoint getBusPoint() {
        return maxPoint;
    }
}
