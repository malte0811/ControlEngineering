package malte0811.controlengineering.gui.tape;

import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.tty.FullSync;
import malte0811.controlengineering.network.tty.TTYPacket;
import malte0811.controlengineering.network.tty.TTYSubPacket;
import malte0811.controlengineering.tiles.tape.TeletypeState;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

public class TeletypeContainer extends CEContainer<TTYSubPacket> {
    private final TeletypeState state;

    public TeletypeContainer(int id, PacketBuffer data) {
        this(id, ContainerScreenManager.readWorldPos(data));
    }

    public TeletypeContainer(int id, IWorldPosCallable pos) {
        super(CEContainers.TELETYPE.get(), pos, id);
        state = pos.apply(World::getTileEntity)
                .filter(t -> t instanceof TeletypeTile)
                .map(t -> ((TeletypeTile) t).getState())
                .orElseGet(TeletypeState::new);
    }

    @Override
    protected SimplePacket makePacket(TTYSubPacket data) {
        return new TTYPacket(data);
    }

    @Override
    protected TTYSubPacket getInitialSync() {
        return new FullSync(state.getAvailable(), state.getData().toByteArray());
    }

    public TeletypeState getState() {
        return state;
    }
}
