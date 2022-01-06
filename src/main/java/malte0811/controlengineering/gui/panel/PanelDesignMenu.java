package malte0811.controlengineering.gui.panel;

import malte0811.controlengineering.blockentity.panels.PanelDesignerBlockEntity;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.CEContainerMenu;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.gui.misc.LambdaDataSlot;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.panellayout.FullSync;
import malte0811.controlengineering.network.panellayout.PanelPacket;
import malte0811.controlengineering.network.panellayout.PanelSubPacket;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;

import java.util.ArrayList;
import java.util.List;

public class PanelDesignMenu extends CEContainerMenu<PanelSubPacket> {
    private final DataSlot requiredLength;
    private final DataSlot availableLength;
    private final List<PlacedComponent> components;

    public PanelDesignMenu(MenuType<?> type, int id) {
        super(type, id);
        this.requiredLength = addDataSlot(DataSlot.standalone());
        this.availableLength = addDataSlot(DataSlot.standalone());
        this.components = new ArrayList<>();
    }

    public PanelDesignMenu(MenuType<?> type, int id, PanelDesignerBlockEntity designer) {
        super(type, id, ContainerScreenManager.isValidFor(designer), designer::setChanged);
        this.requiredLength = addDataSlot(LambdaDataSlot.serverSide(designer::getLengthRequired));
        this.availableLength = addDataSlot(LambdaDataSlot.serverSide(() -> designer.getKeypunch().getAvailable()));
        this.components = designer.getComponents();
    }

    @Override
    protected PanelSubPacket getInitialSync() {
        return new FullSync(components);
    }

    @Override
    protected SimplePacket makePacket(PanelSubPacket panelSubPacket) {
        return new PanelPacket(panelSubPacket);
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    public int getRequiredTapeLength() {
        return requiredLength.get();
    }

    public int getAvailableTapeLength() {
        return availableLength.get();
    }
}
