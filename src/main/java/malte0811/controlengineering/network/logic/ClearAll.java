package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ClearAll extends LogicSubPacket {
    public static final MyCodec<ClearAll> CODEC = MyCodecs.unit(new ClearAll());

    public ClearAll() {}

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        applyTo.clear();
        return true;
    }
}
