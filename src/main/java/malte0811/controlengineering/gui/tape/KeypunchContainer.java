package malte0811.controlengineering.gui.tape;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.FullSync;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import malte0811.controlengineering.network.keypunch.TypeChar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;

public class KeypunchContainer extends CEContainer<KeypunchSubPacket> {
    private final KeypunchState state;
    private final KeypunchBlockEntity keypunchBE;

    public KeypunchContainer(int id, FriendlyByteBuf data) {
        this(id, ContainerScreenManager.readWorldPos(data));
    }

    public KeypunchContainer(int id, ContainerLevelAccess pos) {
        super(CEContainers.KEYPUNCH.get(), pos, id);
        this.keypunchBE = pos.evaluate(Level::getBlockEntity)
                .filter(t -> t instanceof KeypunchBlockEntity)
                .map(be -> (KeypunchBlockEntity) be)
                .orElseGet(() -> new KeypunchBlockEntity(
                        CEBlockEntities.KEYPUNCH.master().get(),
                        BlockPos.ZERO,
                        CEBlocks.KEYPUNCH.get().defaultBlockState()
                ));
        this.state = keypunchBE.getState();
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

    public KeypunchBlockEntity getKeypunchBE() {
        return keypunchBE;
    }

    public boolean isLoopback() {
        return keypunchBE.isLoopback();
    }

    public void onTypedOnServer(byte data) {
        sendToListeningPlayers(new TypeChar(data));
    }

    public void resyncFullTape() {
        sendToListeningPlayers(getInitialSync());
    }

    @Override
    protected void onFirstOpened() {
        super.onFirstOpened();
        keypunchBE.getOpenContainers().add(this);
    }

    @Override
    protected void onLastClosed() {
        super.onLastClosed();
        keypunchBE.getOpenContainers().remove(this);
    }
}
