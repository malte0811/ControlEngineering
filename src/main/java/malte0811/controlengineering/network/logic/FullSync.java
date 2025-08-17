package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class FullSync extends LogicSubPacket {
    public static final MyCodec<FullSync> CODEC = Schematic.CODEC.xmap(FullSync::new, fs -> fs.schematic);

    private final Schematic schematic;

    public FullSync(Schematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        replace.accept(schematic);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
