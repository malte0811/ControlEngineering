package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.network.PacketUtils;
import net.minecraft.network.PacketBuffer;

import java.util.List;

public class FullSync extends PanelSubPacket {
    private final List<PlacedComponent> allComponents;

    public FullSync(List<PlacedComponent> allComponents) {
        this.allComponents = allComponents;
    }

    public FullSync(PacketBuffer buffer) {
        this(PacketUtils.readList(buffer, PlacedComponent::readWithoutState));
    }

    @Override
    protected void write(PacketBuffer out) {
        PacketUtils.writeList(out, allComponents, PlacedComponent::writeToWithoutState);
    }

    @Override
    public boolean process(List<PlacedComponent> allComponents) {
        allComponents.clear();
        allComponents.addAll(this.allComponents);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
