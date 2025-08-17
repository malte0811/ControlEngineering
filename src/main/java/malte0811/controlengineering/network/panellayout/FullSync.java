package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.List;

public record FullSync(List<PlacedComponent> allComponents) implements PanelSubPacket {
    public static final StreamCodec<FriendlyByteBuf, FullSync> CODEC = PlacedComponent.LIST_CODEC.streamCodec().map(
            FullSync::new, FullSync::allComponents
    );

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
