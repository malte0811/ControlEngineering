package malte0811.controlengineering.gui.panel;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.gui.CEContainer;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.panellayout.FullSync;
import malte0811.controlengineering.network.panellayout.PanelPacket;
import malte0811.controlengineering.network.panellayout.PanelSubPacket;
import malte0811.controlengineering.tiles.panels.PanelDesignerTile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;
import java.util.List;

public class PanelDesignContainer extends CEContainer<PanelSubPacket> {
    private final PanelDesignerTile designerTile;

    public PanelDesignContainer(ContainerLevelAccess pos, int id) {
        super(CEContainers.PANEL_DESIGN.get(), pos, id);
        designerTile = pos.evaluate(Level::getBlockEntity)
                .map(te -> te instanceof PanelDesignerTile ? (PanelDesignerTile) te : null)
                .orElseThrow(RuntimeException::new);
        if (designerTile.getLevel().isClientSide())
            designerTile.getKeypunch().setAvailable(0);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return designerTile.getKeypunch().getAvailable();
            }

            @Override
            public void set(int pValue) {
                designerTile.getKeypunch().setAvailable(pValue);
            }
        });
    }

    public PanelDesignContainer(int id, FriendlyByteBuf data) {
        this(ContainerScreenManager.readWorldPos(data), id);
    }

    @Override
    protected PanelSubPacket getInitialSync() {
        return new FullSync(designerTile.getComponents());
    }

    @Override
    protected SimplePacket makePacket(PanelSubPacket panelSubPacket) {
        return new PanelPacket(panelSubPacket);
    }

    public List<PlacedComponent> getComponents() {
        return designerTile.getComponents();
    }

    public int getRequiredTapeLength() {
        return designerTile.getLengthRequired();
    }

    public int getAvailableTapeLength() {
        return designerTile.getKeypunch().getAvailable();
    }
}
