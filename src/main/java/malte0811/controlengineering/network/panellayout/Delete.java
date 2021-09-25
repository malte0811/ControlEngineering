package malte0811.controlengineering.network.panellayout;

import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

public class Delete extends PanelSubPacket {
    private final Vec2d pos;

    public Delete(Vec2d pos) {
        this.pos = pos;
    }

    public Delete(FriendlyByteBuf from) {
        this(new Vec2d(from));
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        pos.write(out);
    }

    @Override
    public boolean process(List<PlacedComponent> allComponents) {
        final int index = PlacedComponent.getIndexAt(allComponents, pos.x, pos.y);
        if (index >= 0) {
            allComponents.remove(index);
            return true;
        } else {
            return false;
        }
    }
}
