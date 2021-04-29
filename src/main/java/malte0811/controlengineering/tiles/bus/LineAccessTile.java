package malte0811.controlengineering.tiles.bus;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.Connection;
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
import malte0811.controlengineering.temp.ImprovedLocalRSHandler;
import malte0811.controlengineering.tiles.CEIICTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public class LineAccessTile extends CEIICTileEntity implements IBusConnector, IRedstoneConnector {
    private static final int REDSTONE_ID = 0;
    private static final int BUS_ID = 1;

    private int selectedLine;
    private BusLine lastLineToRS = new BusLine();
    private BusLine lastLineFromRS = new BusLine();
    private ConnectionPoint redstonePoint;
    private ConnectionPoint busPoint;

    public LineAccessTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        reinitConnectionPoints();
    }

    private void reinitConnectionPoints() {
        redstonePoint = new ConnectionPoint(pos, REDSTONE_ID);
        busPoint = new ConnectionPoint(pos, BUS_ID);
    }

    @Override
    public void setPos(@Nonnull BlockPos posIn) {
        super.setPos(posIn);
        reinitConnectionPoints();
    }

    @Override
    public void setWorldAndPos(World worldIn, BlockPos pos) {
        super.setWorldAndPos(worldIn, pos);
        reinitConnectionPoints();
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        selectedLine = nbt.getInt("selectedLine");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt = super.write(nbt);
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
    public Vector3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here) {
        if (here.getIndex() == REDSTONE_ID) {
            return new Vector3d(0.5, 0.25, 0.5);
        } else {
            return new Vector3d(0.5, 0.75, 0.5);
        }
    }

    @Nullable
    @Override
    public ConnectionPoint getTargetedPoint(TargetingInfo info, Vector3i offset) {
        if (info.hitY > 0.5) {
            return busPoint;
        } else {
            return redstonePoint;
        }
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vector3i offset) {
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
