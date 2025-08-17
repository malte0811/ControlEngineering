package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.List;

public record Delete(Vec2d pos) implements PanelSubPacket {
    public static final StreamCodec<FriendlyByteBuf, Delete> CODEC = Vec2d.CODEC.streamCodec().map(
            Delete::new, Delete::pos
    );

    @Override
    public boolean process(Level level, List<PlacedComponent> allComponents) {
        final int index = PlacedComponent.getIndexAt(level, allComponents, pos.x(), pos.y());
        if (index >= 0) {
            allComponents.remove(index);
            return true;
        } else {
            return false;
        }
    }
}
