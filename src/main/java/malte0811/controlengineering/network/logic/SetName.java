package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SetName extends LogicSubPacket {
    private final String newName;

    public SetName(String newName) {
        this.newName = newName;
    }

    public SetName(FriendlyByteBuf in) {
        this.newName = in.readUtf();
    }

    @Override
    public void write(FriendlyByteBuf out) {
        out.writeUtf(newName);
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        applyTo.setName(newName);
        return true;
    }
}