package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SetName extends LogicSubPacket {
    public static final MyCodec<SetName> CODEC = MyCodecs.STRING.xmap(SetName::new, sn -> sn.newName);

    private final String newName;

    public SetName(String newName) {
        this.newName = newName;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        applyTo.setName(newName);
        return true;
    }
}