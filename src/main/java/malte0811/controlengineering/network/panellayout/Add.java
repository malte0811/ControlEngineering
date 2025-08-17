package malte0811.controlengineering.network.panellayout;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public record Add(@Nullable PlacedComponent toPlace) implements PanelSubPacket {
    public static final StreamCodec<FriendlyByteBuf, Add> CODEC = PlacedComponent.CODEC.streamCodec().map(
            Add::new, Add::toPlace
    );
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
