package malte0811.controlengineering.gui.tape;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import malte0811.controlengineering.blockentity.tape.KeypunchBlockEntity;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.gui.misc.LambdaDataSlot;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.FullSync;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import malte0811.controlengineering.network.keypunch.TypeChar;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;

import java.util.HashSet;
import java.util.Set;

public class KeypunchMenu extends CEContainerMenu<KeypunchSubPacket> {
    private final KeypunchState state;
    private final ByteConsumer printNonLoopback;
    private final DataSlot isLoopback;
    private final Set<KeypunchMenu> openMenus;

    public KeypunchMenu(MenuType<?> type, int id, KeypunchBlockEntity keypunch) {
        super(type, id, ContainerScreenManager.isValidFor(keypunch), keypunch::setChanged);
        this.state = keypunch.getState();
        this.printNonLoopback = keypunch::queueForRemotePrint;
        this.isLoopback = addDataSlot(LambdaDataSlot.serverSide(() -> keypunch.isLoopback() ? 1 : 0));
        this.openMenus = keypunch.getOpenContainers();
    }

    public KeypunchMenu(MenuType<?> type, int id) {
        super(type, id);
        this.state = new KeypunchState(() -> {});
        this.printNonLoopback = $ -> {};
        this.isLoopback = addDataSlot(DataSlot.standalone());
        this.openMenus = new HashSet<>();
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

    @Override
    protected void onFirstOpened() {
        super.onFirstOpened();
        openMenus.add(this);
    }

    @Override
    protected void onLastClosed() {
        super.onLastClosed();
        openMenus.remove(this);
    }

    public ByteConsumer getPrintNonLoopback() {
        return printNonLoopback;
    }
}
