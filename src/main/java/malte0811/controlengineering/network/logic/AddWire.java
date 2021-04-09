package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.WireSegment.WireAxis;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.network.PacketBuffer;

import java.util.function.Consumer;

public class AddWire extends LogicSubPacket {
    private final WireSegment added;

    public AddWire(WireSegment added) {
        this.added = added;
    }

    public AddWire(PacketBuffer buffer) {
        this.added = new WireSegment(
                new Vec2i(buffer), buffer.readVarInt(), buffer.readBoolean() ? WireAxis.X : WireAxis.Y
        );
    }

    @Override
    public void write(PacketBuffer out) {
        this.added.getStart().write(out);
        out.writeVarInt(this.added.getLength());
        out.writeBoolean(this.added.getAxis() == WireAxis.X);
    }

    @Override
    protected void process(Schematic applyTo, Consumer<Schematic> replace) {
        if (applyTo.canAdd(added)) {
            applyTo.addWire(added);
        }
    }
}
