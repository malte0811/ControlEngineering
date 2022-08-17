package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class AddSymbol extends LogicSubPacket {
    public static final MyCodec<AddSymbol> CODEC = PlacedSymbol.CODEC.xmap(AddSymbol::new, as -> as.symbol);

    private final PlacedSymbol symbol;

    public AddSymbol(PlacedSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        return applyTo.addSymbol(symbol, level);
    }
}
