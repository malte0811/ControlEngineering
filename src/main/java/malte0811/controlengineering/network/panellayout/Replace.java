package malte0811.controlengineering.network.panellayout;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;
import java.util.List;

public class Replace extends PanelSubPacket {
    @Nullable
    private final PlacedComponent newComponent;

    public Replace(@Nullable PlacedComponent newComponent) {
        this.newComponent = newComponent;
    }

    public Replace(PacketBuffer buffer) {
        this(PlacedComponent.readWithoutState(buffer));
    }

    @Override
    protected void write(PacketBuffer out) {
        Preconditions.checkNotNull(newComponent).writeToWithoutState(out);
    }

    @Override
    public boolean process(List<PlacedComponent> allComponents) {
        if (newComponent == null || !newComponent.isWithinPanel()) {
            return false;
        }
        int toReplace = -1;
        for (int i = 0; i < allComponents.size(); i++) {
            PlacedComponent existing = allComponents.get(i);
            if (existing.getPosMin().equals(newComponent.getPosMin())) {
                toReplace = i;
            } else if (!existing.disjoint(newComponent)) {
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
