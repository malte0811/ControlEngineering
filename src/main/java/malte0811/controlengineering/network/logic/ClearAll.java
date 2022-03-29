package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class ClearAll extends LogicSubPacket {
    public ClearAll() {}

    public ClearAll(FriendlyByteBuf buffer) {}

    @Override
    protected void write(FriendlyByteBuf out) {}

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        applyTo.clear();
        return true;
    }
}
