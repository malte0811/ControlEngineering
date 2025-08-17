package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public abstract class LogicSubPacket {
    public abstract boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level);

    public boolean allowSendingToServer() {
        return true;
    }

    public boolean canApplyOnReadOnly() {
        return false;
    }
}
