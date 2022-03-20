package malte0811.controlengineering.network.panellayout;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class Add extends PanelSubPacket {
    @Nullable
    private final PlacedComponent toPlace;

    public Add(@Nullable PlacedComponent toPlace) {
        this.toPlace = toPlace;
    }

    public Add(FriendlyByteBuf buffer) {
        this(PlacedComponent.readWithoutState(buffer));
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        Preconditions.checkNotNull(toPlace);
        toPlace.writeToWithoutState(out);
    }

    @Override
    public boolean process(Level level, List<PlacedComponent> allComponents) {
        if (toPlace == null) {
            return false;
        }
        if (!toPlace.isWithinPanel(level)) {
            return false;
        }
        for (PlacedComponent existing : allComponents) {
            if (!existing.disjoint(level, toPlace)) {
                return false;
            }
        }
        allComponents.add(toPlace);
        return true;
    }
}
