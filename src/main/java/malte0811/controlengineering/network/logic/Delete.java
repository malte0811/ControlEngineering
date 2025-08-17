package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class Delete extends LogicSubPacket {
    public static final MyCodec<Delete> CODEC = Vec2d.CODEC.xmap(Delete::new, d -> d.pos);

    private final Vec2d pos;

    public Delete(Vec2d pos) {
        this.pos = pos;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        return applyTo.removeOneContaining(pos, level);
    }
}
