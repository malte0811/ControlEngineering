package malte0811.controlengineering.bus;

import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.api.wires.localhandlers.IWorldTickable;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class LocalBusHandler extends LocalNetworkHandler implements IWorldTickable {
    public static final ResourceLocation NAME = new ResourceLocation(ControlEngineering.MODID, "bus");
    private Map<ConnectionPoint, BusState> outputByBlock = new HashMap<>();
    private BusState state;
    private boolean updateNextTick = true;

    public LocalBusHandler(LocalWireNetwork local, GlobalWireNetwork global) {
        super(local, global);
    }

    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler other) {
        if (!(other instanceof LocalBusHandler))
            return new LocalBusHandler(localNet, globalNet);
        state = state.merge(((LocalBusHandler) other).state);
        requestUpdate();
        return this;
    }

    @Override
    public void onConnectorLoaded(ConnectionPoint p, IImmersiveConnectable iic) {
        requestUpdate();
    }

    @Override
    public void onConnectorUnloaded(BlockPos p, IImmersiveConnectable iic) {
        requestUpdate();
    }

    @Override
    public void onConnectorRemoved(BlockPos p, IImmersiveConnectable iic) {
        requestUpdate();
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
        return state;
    }

    public void requestUpdate() {
        this.updateNextTick = true;
    }

    @Override
    public void update(World w) {
        if (updateNextTick) {
            outputByBlock.clear();
            final BusState oldState = state;
            state = null;
            List<Pair<ConnectionPoint, IBusConnector>> busConnections = new ArrayList<>();
            for (ConnectionPoint cp : localNet.getConnectionPoints()) {
                IImmersiveConnectable iic = localNet.getConnector(cp);
                if (iic instanceof IBusConnector) {
                    busConnections.add(Pair.of(cp, (IBusConnector) iic));
                    final BusState emittedState = ((IBusConnector) iic).getEmittedState(cp);
                    outputByBlock.put(cp, emittedState);
                    if (state == null) {
                        state = emittedState;
                    } else {
                        state = state.merge(emittedState);
                    }
                }
            }
            if (!oldState.equals(Objects.requireNonNull(state))) {
                for (Pair<ConnectionPoint, IBusConnector> entry : busConnections) {
                    entry.getSecond().onBusUpdated(entry.getFirst());
                }
            }
            this.updateNextTick = false;
        }
    }
}
