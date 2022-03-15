package malte0811.controlengineering.gui.remapper;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.bus.RSRemapperBlockEntity;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.remapper.FullSync;
import malte0811.controlengineering.network.remapper.RSRemapperPacket;
import malte0811.controlengineering.network.remapper.RSRemapperSubPacket;
import net.minecraft.world.inventory.MenuType;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RSRemapperMenu extends CEContainerMenu<RSRemapperSubPacket> {
    private final Consumer<int[]> setColorToGray;
    private final Supplier<int[]> getColorToGray;

    public RSRemapperMenu(MenuType<?> type, int id, RSRemapperBlockEntity bEntity) {
        super(type, id, ContainerScreenManager.isValidFor(bEntity), bEntity::setChanged);
        this.getColorToGray = bEntity::getColorToGray;
        this.setColorToGray = bEntity::setColorToGray;
    }

    public RSRemapperMenu(MenuType<?> type, int id) {
        super(type, id);
        Mutable<int[]> colorToGray = new MutableObject<>(new int[BusLine.LINE_SIZE]);
        this.getColorToGray = colorToGray::getValue;
        this.setColorToGray = colorToGray::setValue;
    }

    @Override
    protected SimplePacket makePacket(RSRemapperSubPacket subPacket) {
        return new RSRemapperPacket(subPacket);
    }

    @Override
    protected RSRemapperSubPacket getInitialSync() {
        return new FullSync(getColorToGray());
    }

    public void setColorToGray(int[] newCToG) {
        this.setColorToGray.accept(newCToG);
    }

    public int[] getColorToGray() {
        var original = this.getColorToGray.get();
        return Arrays.copyOf(original, original.length);
    }

    public void processAndSend(RSRemapperSubPacket packet) {
        var fullPacket = new RSRemapperPacket(packet);
        fullPacket.updateConnections(this);
        ControlEngineering.NETWORK.sendToServer(fullPacket);
    }
}
