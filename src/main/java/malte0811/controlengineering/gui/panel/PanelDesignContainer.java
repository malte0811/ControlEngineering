package malte0811.controlengineering.gui.panel;

import malte0811.controlengineering.blockentity.panels.PanelDesignerBlockEntity;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.panellayout.FullSync;
import malte0811.controlengineering.network.panellayout.PanelPacket;
import malte0811.controlengineering.network.panellayout.PanelSubPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;

import java.util.List;

public class PanelDesignContainer extends CEContainer<PanelSubPacket> {
    private final PanelDesignerBlockEntity designerBE;

    public PanelDesignContainer(ContainerLevelAccess pos, int id) {
        super(CEContainers.PANEL_DESIGN.get(), pos, id);
        designerBE = pos.evaluate(Level::getBlockEntity)
                .map(be -> be instanceof PanelDesignerBlockEntity designer ? designer : null)
                .orElseThrow(RuntimeException::new);
        if (designerBE.getLevel().isClientSide())
            designerBE.getKeypunch().setAvailable(0);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return designerBE.getKeypunch().getAvailable();
            }

            @Override
            public void set(int pValue) {
                designerBE.getKeypunch().setAvailable(pValue);
            }
        });
    }

    public PanelDesignContainer(int id, FriendlyByteBuf data) {
        this(ContainerScreenManager.readWorldPos(data), id);
    }

    @Override
    protected PanelSubPacket getInitialSync() {
        return new FullSync(designerBE.getComponents());
    }

    @Override
    protected SimplePacket makePacket(PanelSubPacket panelSubPacket) {
        return new PanelPacket(panelSubPacket);
    }

    public List<PlacedComponent> getComponents() {
        return designerBE.getComponents();
    }

    public int getRequiredTapeLength() {
        return designerBE.getLengthRequired();
    }

    public int getAvailableTapeLength() {
        return designerBE.getKeypunch().getAvailable();
    }
}
