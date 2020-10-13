package malte0811.controlengineering.temp;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ImprovedLocalRSHandler extends RedstoneNetworkHandler {
    public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "redstone");
    private byte[] totalValues = new byte[16];
    private final Map<ConnectionPoint, byte[]> emittedValues = new HashMap<>();

    public ImprovedLocalRSHandler(LocalWireNetwork local, GlobalWireNetwork global) {
        super(local, global);
    }

    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler other) {
        if (!(other instanceof ImprovedLocalRSHandler))
            return new ImprovedLocalRSHandler(localNet, globalNet);
        ImprovedLocalRSHandler otherRS = (ImprovedLocalRSHandler) other;
        ImprovedLocalRSHandler ret = new ImprovedLocalRSHandler(localNet, globalNet);
        fillWithMax(totalValues, otherRS.totalValues, ret.totalValues);
        ret.emittedValues.putAll(this.emittedValues);
        ret.emittedValues.putAll(otherRS.emittedValues);
        return ret;
    }

    @Override
    public void onConnectorLoaded(ConnectionPoint newCP, IImmersiveConnectable iic) {
        if (!(iic instanceof IRedstoneConnector))
            return;
        localNet.addAsFutureTask(() -> {
            IRedstoneConnector rsConn = (IRedstoneConnector) iic;
            byte[] emitted = getEmitted(rsConn, newCP);
            fillWithMax(emitted, totalValues, totalValues);
            emittedValues.put(newCP, emitted);
            for (ConnectionPoint cp : localNet.getConnectionPoints()) {
                IImmersiveConnectable here = localNet.getConnector(cp);
                if (here instanceof IRedstoneConnector)
                    ((IRedstoneConnector) here).onChange(cp, this);
            }
        });
    }

    public void updateValues() {
        totalValues = new byte[16];
        emittedValues.clear();
        for (ConnectionPoint cp : localNet.getConnectionPoints()) {
            IImmersiveConnectable here = localNet.getConnector(cp);
            if (here instanceof IRedstoneConnector) {
                byte[] output = getEmitted((IRedstoneConnector) here, cp);
                emittedValues.put(cp, output);
                fillWithMax(output, totalValues, totalValues);
            }
        }
        for (ConnectionPoint cp : localNet.getConnectionPoints()) {
            IImmersiveConnectable here = localNet.getConnector(cp);
            if (here instanceof IRedstoneConnector)
                ((IRedstoneConnector) here).onChange(cp, this);
        }
    }

    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        updateValues();
    }

    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        updateValues();
    }

    @Override
    public void onConnectionAdded(Connection c) {
    }

    @Override
    public void onConnectionRemoved(Connection c) {
    }

    @Override
    public void setLocalNet(LocalWireNetwork net) {
        super.setLocalNet(net);
        for (ConnectionPoint cp : net.getConnectionPoints()) {
            IImmersiveConnectable here = net.getConnector(cp);
            if (here instanceof IRedstoneConnector)
                ((IRedstoneConnector) here).onChange(cp, this);
        }
    }

    public byte getValue(int redstoneChannel) {
        return totalValues[redstoneChannel];
    }

    public byte[] getValuesWithout(ConnectionPoint excluded) {
        byte[] ret = new byte[16];
        for (Map.Entry<ConnectionPoint, byte[]> entry : emittedValues.entrySet()) {
            if (!entry.getKey().equals(excluded)) {
                fillWithMax(entry.getValue(), ret, ret);
            }
        }
        return ret;
    }

    private static byte[] getEmitted(IRedstoneConnector connector, ConnectionPoint cp) {
        byte[] ret = new byte[16];
        connector.updateInput(ret, cp);
        return ret;
    }

    private static void fillWithMax(byte[] inA, byte[] inB, byte[] out) {
        Preconditions.checkArgument(inA.length == 16);
        Preconditions.checkArgument(inB.length == 16);
        Preconditions.checkArgument(out.length == 16);
        for (int i = 0; i < 16; ++i)
            out[i] = (byte) Math.max(inA[i], inB[i]);
    }
}
