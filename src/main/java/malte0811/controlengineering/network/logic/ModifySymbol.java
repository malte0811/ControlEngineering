package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class ModifySymbol extends LogicSubPacket {
    private final PlacedSymbol newSymbol;

    public ModifySymbol(PlacedSymbol newSymbol) {
        this.newSymbol = newSymbol;
    }

    public ModifySymbol(FriendlyByteBuf in) {
        this(PlacedSymbol.CODEC.from(in));
    }

    @Override
    protected void write(FriendlyByteBuf out) {
        PlacedSymbol.CODEC.toSerial(new PacketBufferStorage(out), newSymbol);
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        List<PlacedSymbol> symbols = applyTo.getSymbols();
        for (int i = 0; i < symbols.size(); i++) {
            PlacedSymbol oldSymbol = symbols.get(i);
            if (oldSymbol.symbol().getType() != newSymbol.symbol().getType()) {
                continue;
            } else if (!oldSymbol.position().equals(newSymbol.position())) {
                continue;
            }
            return applyTo.replaceBy(i, newSymbol, level);
        }
        return false;
    }

    @Override
    public boolean canApplyOnReadOnly() {
        return newSymbol.symbol().getType().canConfigureOnReadOnly();
    }
}
