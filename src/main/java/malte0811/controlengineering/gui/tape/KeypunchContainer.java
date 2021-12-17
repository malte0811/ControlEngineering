package malte0811.controlengineering.gui.tape;

import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.FullSync;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public class KeypunchContainer extends CEContainer<KeypunchSubPacket> {
    private final KeypunchState state;

    public KeypunchContainer(int id, FriendlyByteBuf data) {
        this(id, ContainerScreenManager.readWorldPos(data));
    }

    public KeypunchContainer(int id, ContainerLevelAccess pos) {
        super(CEContainers.KEYPUNCH.get(), pos, id);
        state = pos.evaluate(Level::getBlockEntity)
                .filter(t -> t instanceof KeypunchBlockEntity)
                .map(t -> ((KeypunchBlockEntity) t).getState())
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
