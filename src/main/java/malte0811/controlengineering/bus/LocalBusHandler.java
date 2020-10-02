package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LocalBusHandler extends LocalNetworkHandler implements IWorldTickable {
    public static final ResourceLocation NAME = new ResourceLocation(ControlEngineering.MODID, "bus");
    private boolean updateNextTick = true;
    private final BusEmitterCombiner<Pair<ConnectionPoint, IBusConnector>> stateHandler = new BusEmitterCombiner<>(
            pair -> pair.getSecond().getEmittedState(pair.getFirst()),
            pair -> pair.getSecond().onBusUpdated(pair.getFirst())
    );

    public LocalBusHandler(LocalWireNetwork local, GlobalWireNetwork global) {
        super(local, global);
        for (ConnectionPoint cp : local.getConnectionPoints()) {
            IImmersiveConnectable iic = local.getConnector(cp);
            if (iic instanceof IBusConnector) {
                stateHandler.addEmitter(Pair.of(cp, (IBusConnector) iic));
            }
        }
    }

    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler other) {
        if (!(other instanceof LocalBusHandler))
            return new LocalBusHandler(localNet, globalNet);
        for (Pair<ConnectionPoint, IBusConnector> pair : ((LocalBusHandler) other).stateHandler.getEmitters()) {
                stateHandler.addEmitter(pair);
        }
        requestUpdate();
        return this;
    }

    @Override
    public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic) {
        requestUpdate();
        if (iic instanceof IBusConnector) {
            stateHandler.addEmitter(Pair.of(p, (IBusConnector) iic));
        }
    }

    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        onConnectorRemoved(p, iic);
    }

    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        requestUpdate();
        if (iic instanceof IBusConnector) {
            for (ConnectionPoint cp : localNet.getConnectionPoints()) {
                if (cp.getPosition().equals(p)) {
                    stateHandler.removeEmitter(Pair.of(cp, (IBusConnector) iic));
                }
            }
        }
    }

    @Override
    public void onConnectionAdded(Connection c) {
        requestUpdate();
    }

    @Override
    public void onConnectionRemoved(Connection c) {
        requestUpdate();
    }

    public BusState getState() {
        return stateHandler.getTotalState();
    }

    public void requestUpdate() {
        this.updateNextTick = true;
    }

    @Override
    public void update(World w) {
        if (updateNextTick) {
            stateHandler.updateState(new BusState());
        }
    }
}
