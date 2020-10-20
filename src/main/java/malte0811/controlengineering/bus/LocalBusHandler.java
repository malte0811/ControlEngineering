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
            loadConnectionPoint(cp, iic);
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
        loadConnectionPoint(p, iic);
    }

    private void loadConnectionPoint(ConnectionPoint cp, IImmersiveConnectable iic) {
        if (iic instanceof IBusConnector && ((IBusConnector) iic).isBusPoint(cp)) {
            stateHandler.addEmitter(Pair.of(cp, (IBusConnector) iic));
        }
    }

    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        onConnectorRemoved(p, iic);
    }

    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        if (iic instanceof IBusConnector) {
            IBusConnector busConnector = (IBusConnector) iic;
            for (ConnectionPoint cp : busConnector.getConnectionPoints()) {
                if (busConnector.isBusPoint(cp)) {
                    stateHandler.removeEmitterIfPresent(Pair.of(cp, busConnector));
                }
            }
            requestUpdate();
        }
    }

    @Override
    public void onConnectionAdded(Connection c) {}

    @Override
    public void onConnectionRemoved(Connection c) {}

    public BusState getState() {
        return stateHandler.getTotalState();
    }

    public BusState getStateWithout(ConnectionPoint excluded, IBusConnector connector) {
        return stateHandler.getStateWithout(Pair.of(excluded, connector));
    }

    public void requestUpdate() {
        this.updateNextTick = true;
    }

    @Override
    public void update(World w) {
        if (updateNextTick) {
            stateHandler.updateState(BusState.EMPTY);
            updateNextTick = false;
        }
    }
}
