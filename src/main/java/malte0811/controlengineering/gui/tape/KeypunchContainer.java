package malte0811.controlengineering.gui.tape;

import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.FullSync;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import malte0811.controlengineering.tiles.tape.KeypunchState;
import malte0811.controlengineering.tiles.tape.KeypunchTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

public class KeypunchContainer extends CEContainer<KeypunchSubPacket> {
    private final KeypunchState state;

    public KeypunchContainer(int id, PacketBuffer data) {
        this(id, ContainerScreenManager.readWorldPos(data));
    }

    public KeypunchContainer(int id, IWorldPosCallable pos) {
        super(CEContainers.KEYPUNCH.get(), pos, id);
        state = pos.apply(World::getTileEntity)
                .filter(t -> t instanceof KeypunchTile)
                .map(t -> ((KeypunchTile) t).getState())
                .orElseGet(KeypunchState::new);
    }

    @Override
    protected SimplePacket makePacket(KeypunchSubPacket data) {
        return new KeypunchPacket(data);
    }

    @Override
    protected KeypunchSubPacket getInitialSync() {
        return new FullSync(state.getAvailable(), state.getData().toByteArray());
    }

    public KeypunchState getState() {
        return state;
    }
}
