package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AddSymbol extends LogicSubPacket {
    private final PlacedSymbol symbol;

    public AddSymbol(PlacedSymbol symbol) {
        this.symbol = symbol;
    }

    public AddSymbol(FriendlyByteBuf in) {
        this(PlacedSymbol.CODEC.fromSerial(new PacketBufferStorage(in)).get());
    }

    @Override
    public void write(FriendlyByteBuf out) {
        PlacedSymbol.CODEC.toSerial(new PacketBufferStorage(out), symbol);
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        return applyTo.addSymbol(symbol, level);
    }
}
