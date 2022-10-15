package malte0811.controlengineering.gui.tape;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.gui.misc.LambdaDataSlot;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.FullSync;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import malte0811.controlengineering.network.keypunch.TypeChar;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;

public class KeypunchMenu extends CEContainerMenu<KeypunchSubPacket> {
    private final KeypunchState state;
    private final ByteConsumer printNonLoopback;
    private final DataSlot isLoopback;

    public KeypunchMenu(MenuType<?> type, int id, KeypunchBlockEntity keypunch) {
        super(type, id, isValidFor(keypunch), keypunch::setChanged, keypunch.getOpenContainers());
        this.state = keypunch.getState();
        this.printNonLoopback = keypunch::queueForRemotePrint;
        this.isLoopback = addDataSlot(LambdaDataSlot.serverSide(() -> keypunch.isLoopback() ? 1 : 0));
    }

    public KeypunchMenu(MenuType<?> type, int id) {
        super(type, id);
        this.state = new KeypunchState(() -> {});
        this.printNonLoopback = $ -> {};
        this.isLoopback = addDataSlot(DataSlot.standalone());
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

    public boolean isLoopback() {
        return isLoopback.get() != 0;
    }

    public void onTypedOnServer(byte data) {
        sendToListeningPlayers(new TypeChar(data));
    }

    public void resyncFullTape() {
        sendToListeningPlayers(getInitialSync());
    }

    public ByteConsumer getPrintNonLoopback() {
        return printNonLoopback;
    }
}
