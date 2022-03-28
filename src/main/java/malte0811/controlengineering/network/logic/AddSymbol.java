package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.serialization.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AddSymbol extends LogicSubPacket {
    private final PlacedSymbol symbol;

    public AddSymbol(PlacedSymbol symbol) {
        this.symbol = symbol;
    }

    public AddSymbol(FriendlyByteBuf in) {
        this.symbol = new PlacedSymbol(
                new Vec2i(in), SymbolInstance.CODEC.fromSerial(new PacketBufferStorage(in)).get()
        );
    }

    @Override
    public void write(FriendlyByteBuf out) {
        symbol.position().write(out);
        SymbolInstance.CODEC.toSerial(new PacketBufferStorage(out), symbol.symbol());
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        if (applyTo.makeChecker(level).canAdd(symbol)) {
            applyTo.addSymbol(symbol);
            return true;
        }
        return false;
    }
}
