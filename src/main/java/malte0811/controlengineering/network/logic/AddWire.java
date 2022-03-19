package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.WireSegment.WireAxis;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class AddWire extends LogicSubPacket {
    private final WireSegment added;

    public AddWire(WireSegment added) {
        this.added = added;
    }

    public AddWire(FriendlyByteBuf buffer) {
        this.added = new WireSegment(
                new Vec2i(buffer), buffer.readVarInt(), buffer.readBoolean() ? WireAxis.X : WireAxis.Y
        );
    }

    @Override
    public void write(FriendlyByteBuf out) {
        this.added.start().write(out);
        out.writeVarInt(this.added.length());
        out.writeBoolean(this.added.axis() == WireAxis.X);
    }

    @Override
    public void process(Schematic applyTo, Consumer<Schematic> replace) {
        if (applyTo.getChecker().canAdd(added)) {
            applyTo.addWire(added);
        }
    }
}
