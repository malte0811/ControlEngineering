package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.function.Consumer;

public class AddSymbol extends LogicSubPacket {
    private final PlacedSymbol symbol;

    public AddSymbol(PlacedSymbol symbol) {
        this.symbol = symbol;
    }

    public AddSymbol(PacketBuffer in) {
        try {
            this.symbol = new PlacedSymbol(new Vec2i(in), in.func_240628_a_(SymbolInstance.CODEC));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(PacketBuffer out) {
        symbol.getPosition().write(out);
        try {
            out.func_240629_a_(SymbolInstance.CODEC, symbol.getSymbol());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void process(Schematic applyTo, Consumer<Schematic> replace) {
        if (applyTo.getChecker().canAdd(symbol)) {
            applyTo.addSymbol(symbol);
        }
    }
}
