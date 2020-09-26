package malte0811.controlengineering.tiles.bus;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.bus.IBusConnector;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.tiles.CETileEntities;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LineAccessTile extends ImmersiveConnectableTileEntity implements IBusConnector, IRedstoneConnector {
    private static final int REDSTONE_ID = 0;
    private static final int BUS_ID = 1;

    private int selectedLine;
    private BusLine lastLine = new BusLine();
    private ConnectionPoint redstonePoint;
    private ConnectionPoint busPoint;

    public LineAccessTile() {
        super(CETileEntities.LINE_ACCESS.get());
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
    public void readCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        selectedLine = nbt.getInt("selectedLine");
    }

    @Override
    public void writeCustomNBT(@Nonnull CompoundNBT nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putInt("selectedLine", selectedLine);
    }

    /*BUS*/
    @Override
    public int getMinBusWidthForConfig(ConnectionPoint cp) {
        return selectedLine + 1;
    }

    @Override
    public void onBusUpdated(ConnectionPoint updatedPoint) {
        RedstoneNetworkHandler rsHandler = getRSNet();
        if (rsHandler != null && !this.lastLine.equals(new BusLine(rsHandler))) {
            rsHandler.updateValues();
        }
    }

    @Override
    public BusState getEmittedState(ConnectionPoint checkedPoint) {
        BusState ret = new BusState(getActualBusWidth(checkedPoint));
        RedstoneNetworkHandler rs = getRSNet();
        if (rs != null) {
            ret = ret.withLine(selectedLine, new BusLine(rs));
        }
        return ret;
    }

    @Override
    public LocalWireNetwork getLocalNet(int cpIndex) {
        return super.getLocalNet(cpIndex);
    }

    /*GENERAL ICC*/
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
    public boolean canConnectCable(
            WireType wireType, ConnectionPoint connectionPoint, Vector3i offset
    ) {
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
        getBusNet().requestUpdate();
    }

    @Override
    public void updateInput(byte[] signals, ConnectionPoint cp) {
        BusLine line = getBusNet().getState().getLine(selectedLine);
        for (int i = 0; i < signals.length; ++i) {
            signals[i] = (byte) Math.max(line.getValue(i), signals[i]);
        }
    }

    @Nullable
    private RedstoneNetworkHandler getRSNet() {
        return getLocalNet(REDSTONE_ID)
                .getHandler(RedstoneNetworkHandler.ID, RedstoneNetworkHandler.class);
    }

    private LocalBusHandler getBusNet() {
        return getBusHandler(new ConnectionPoint(pos, BUS_ID));
    }
}
