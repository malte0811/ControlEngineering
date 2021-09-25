package malte0811.controlengineering.tiles.bus;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.temp.ImprovedLocalRSHandler;
import malte0811.controlengineering.tiles.CEIICTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class LineAccessTile extends CEIICTileEntity implements IBusConnector, IRedstoneConnector {
    private static final int REDSTONE_ID = 0;
    private static final int BUS_ID = 1;

    public int selectedLine;
    private BusLine lastLineToRS = new BusLine();
    private ConnectionPoint redstonePoint;
    private ConnectionPoint busPoint;

    public LineAccessTile(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        reinitConnectionPoints();
    }

    private void reinitConnectionPoints() {
        redstonePoint = new ConnectionPoint(worldPosition, REDSTONE_ID);
        busPoint = new ConnectionPoint(worldPosition, BUS_ID);
    }

    @Override
    public void setPosition(@Nonnull BlockPos posIn) {
        super.setPosition(posIn);
        reinitConnectionPoints();
    }

    @Override
    public void setLevelAndPosition(@Nonnull Level worldIn, @Nonnull BlockPos pos) {
        super.setLevelAndPosition(worldIn, pos);
        reinitConnectionPoints();
    }

    @Override
    public void load(@Nonnull BlockState state, @Nonnull CompoundTag nbt) {
        super.load(state, nbt);
        selectedLine = nbt.getInt("selectedLine");
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        nbt = super.save(nbt);
        nbt.putInt("selectedLine", selectedLine);
        return nbt;
    }

    /*BUS*/
    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {
        RedstoneNetworkHandler rsHandler = getRSNet();
        BusLine lineToRS = getBusNet().getStateWithout(busPoint, this).getLine(selectedLine);
        if (rsHandler != null && !this.lastLineToRS.equals(lineToRS)) {
            rsHandler.updateValues();
            this.lastLineToRS = lineToRS;
        }
    }

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        BusState ret = BusState.EMPTY;
        ImprovedLocalRSHandler rs = getRSNet();
        if (rs != null) {
            ret = ret.withLine(selectedLine, BusLine.fromRSState(rs.getValuesWithout(redstonePoint)));
        }
        return ret;
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    /*GENERAL IIC*/
    @Override
    public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here) {
        final double offset;
        if (here.getIndex() == REDSTONE_ID) {
            offset = .25;
        } else {
            offset = -.25;
        }
        return new Vec3(0.5, 7 / 16., 0.5).add(
                Vec3.atLowerCornerOf(getBlockState().getValue(LineAccessBlock.FACING).getNormal())
                        .scale(offset)
        );
    }

    @Nullable
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo info, Vec3i offset) {
        Direction facing = getBlockState().getValue(LineAccessBlock.FACING);
        Vec3i normal = facing.getNormal();
        if (normal.getX() * (info.hitX - .5) + normal.getY() * (info.hitY - .5) + normal.getZ() * (info.hitZ - .5) < 0) {
            return busPoint;
        } else {
            return redstonePoint;
        }
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vec3i offset) {
        //TODO only allow one connection
        if (connectionPoint.getIndex() == BUS_ID) {
            return IBusConnector.super.canConnectCable(wireType, connectionPoint, offset);
        } else {
            return wireType.getCategory().equals(WireType.REDSTONE_CATEGORY);
        }
    }

    /*REDSTONE*/
    @Override
    public void onChange(ConnectionPoint cp, RedstoneNetworkHandler handler) {
        //TODO more intelligent behavior?
        if (redstonePoint.equals(cp)) {
            getBusNet().requestUpdate();
        }
    }

    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        if (redstonePoint.equals(cp)) {
            BusLine line = getBusNet().getStateWithout(busPoint, this).getLine(selectedLine);
            for (int i = 0; i < signals.length; ++i) {
                signals[i] = (byte) Math.max(line.getRSValue(i), signals[i]);
            }
        }
    }

    @Nullable
    private ImprovedLocalRSHandler getRSNet() {
        return getLocalNet(REDSTONE_ID)
                .getHandler(RedstoneNetworkHandler.ID, ImprovedLocalRSHandler.class);
    }

    private LocalBusHandler getBusNet() {
        return Objects.requireNonNull(getBusHandler(busPoint));
    }

    @Override
    public Collection<ConnectionPoint> getConnectionPoints() {
        return ImmutableList.of(redstonePoint, busPoint);
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return ImmutableList.of(LocalBusHandler.NAME, RedstoneNetworkHandler.ID);
    }

    @Override
    public boolean isBusPoint(ConnectionPoint cp) {
        return busPoint.equals(cp);
    }
}
