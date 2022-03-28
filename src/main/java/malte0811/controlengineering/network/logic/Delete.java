package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class Delete extends LogicSubPacket {
    private final Vec2d pos;

    public Delete(Vec2d pos) {
        this.pos = pos;
    }

    public Delete(FriendlyByteBuf buffer) {
        this(new Vec2d(buffer));
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        pos.write(out);
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        return applyTo.removeOneContaining(pos, level);
    }
}
