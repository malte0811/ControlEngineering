package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.List;

public class FullSync extends PanelSubPacket {
    private final List<PlacedComponent> allComponents;

    public FullSync(List<PlacedComponent> allComponents) {
        this.allComponents = allComponents;
    }

    public FullSync(FriendlyByteBuf buffer) {
        this(PacketUtils.readList(buffer, PlacedComponent::readWithoutState));
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        PacketUtils.writeList(out, allComponents, PlacedComponent::writeToWithoutState);
    }

    @Override
    public boolean process(Level level, List<PlacedComponent> allComponents) {
        allComponents.clear();
        allComponents.addAll(this.allComponents);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
