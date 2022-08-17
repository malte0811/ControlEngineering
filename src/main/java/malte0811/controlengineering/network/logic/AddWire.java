package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AddWire extends LogicSubPacket {
    public static final MyCodec<AddWire> CODEC = WireSegment.CODEC.xmap(AddWire::new, aw -> aw.added);

    private final WireSegment added;

    public AddWire(WireSegment added) {
        this.added = added;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        if (applyTo.makeChecker(level).canAdd(added)) {
            applyTo.addWire(added);
            return true;
        }
        return false;
    }
}
