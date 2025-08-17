package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.List;

public record Replace(PlacedComponent newComponent) implements PanelSubPacket {
    public static final StreamCodec<FriendlyByteBuf, Replace> CODEC = PlacedComponent.CODEC.streamCodec().map(
            Replace::new, Replace::newComponent
    );

    @Override
    public boolean process(Level level, List<PlacedComponent> allComponents) {
        if (newComponent == null || !newComponent.isWithinPanel(level)) {
            return false;
        }
        int toReplace = -1;
        for (int i = 0; i < allComponents.size(); i++) {
            PlacedComponent existing = allComponents.get(i);
            if (existing.getPosMin().equals(newComponent.getPosMin())) {
                toReplace = i;
            } else if (!existing.disjoint(level, newComponent)) {
                return false;
            }
        }
        if (toReplace < 0) {
            return false;
        }
        allComponents.set(toReplace, newComponent);
        return true;
    }
}
