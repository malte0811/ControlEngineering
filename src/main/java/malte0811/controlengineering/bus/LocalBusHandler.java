package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class LocalBusHandler extends LocalNetworkHandler implements IWorldTickable {
    public static final ResourceLocation NAME = new ResourceLocation(ControlEngineering.MODID, "bus");
    private boolean updateNextTick = true;
    private final List<ConnectionPoint> loading = new ArrayList<>();
    private final BusEmitterCombiner<Pair<ConnectionPoint, IBusConnector>> stateHandler = new BusEmitterCombiner<>(
            pair -> pair.getSecond().getEmittedState(pair.getFirst()),
            pair -> pair.getSecond().onBusUpdated(pair.getFirst())
    );

    public LocalBusHandler(LocalWireNetwork local, GlobalWireNetwork global) {
        super(local, global);
        loading.addAll(local.getConnectionPoints());
    }

    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler other) {
        if (!(other instanceof LocalBusHandler otherBus))
            return new LocalBusHandler(localNet, globalNet);
        for (Pair<ConnectionPoint, IBusConnector> pair : otherBus.stateHandler.getEmitters()) {
            stateHandler.addEmitter(pair);
        }
        requestUpdate();
        return this;
    }

    @Override
    public void setLocalNet(LocalWireNetwork net) {
        super.setLocalNet(net);
        stateHandler.clear();
        loading.addAll(localNet.getConnectionPoints());
        requestUpdate();
    }

    @Override
    public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic) {
        loading.add(p);
        requestUpdate();
    }

    private void loadConnectionPoint(ConnectionPoint cp) {
        var iic = localNet.getConnector(cp);
        if (iic instanceof IBusConnector busIIC && busIIC.isBusPoint(cp)) {
            stateHandler.addEmitter(Pair.of(cp, busIIC));
        }
    }

    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        onConnectorRemoved(p, iic);
    }

    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        if (iic instanceof IBusConnector busConnector) {
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
    public void update(Level w) {
        if (!loading.isEmpty()) {
            for (var point : loading) {
                loadConnectionPoint(point);
            }
        }
        if (updateNextTick) {
            stateHandler.updateState(BusState.EMPTY);
            updateNextTick = false;
        }
    }
}
