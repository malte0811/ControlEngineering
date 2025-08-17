package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.List;

public interface PanelSubPacket {
    boolean process(Level level, List<PlacedComponent> allComponents);

    default boolean allowSendingToServer() {
        return true;
    }
}
