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
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

import java.util.List;

public class PanelLayoutContainer extends CEContainer<PanelSubPacket> {
    private final List<PlacedComponent> components;

    public PanelLayoutContainer(IWorldPosCallable pos, int id) {
        super(CEContainers.PANEL_LAYOUT.get(), pos, id);
        components = pos.apply(World::getTileEntity)
                .map(te -> te instanceof PanelDesignerTile ? (PanelDesignerTile) te : null)
                .map(PanelDesignerTile::getComponents)
                .orElseThrow(RuntimeException::new);
    }

    public PanelLayoutContainer(int id, PacketBuffer data) {
        this(ContainerScreenManager.readWorldPos(data), id);
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
}
