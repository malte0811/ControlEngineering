package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class Add extends LogicSubPacket {
    public static final MyCodec<Add> CODEC = new RecordCodec2<>(
            MyCodecs.list(WireSegment.CODEC).fieldOf("segments", a  -> a.segments),
            MyCodecs.list(PlacedSymbol.CODEC).fieldOf("placedSymbol", a  -> a.symbols),
            Add::new
    );

    private final List<WireSegment> segments;
    private final List<PlacedSymbol> symbols;

    public Add(List<WireSegment> segments, List<PlacedSymbol> symbols) {
        this.segments = segments;
        this.symbols = symbols;
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        if (applyTo.makeChecker(level).getErrorForAddingAll(symbols, segments).isEmpty()) {
            for (final var wire : segments) { applyTo.addWire(wire); }
            for (final var symbol : symbols) { applyTo.addSymbol(symbol); }
            return true;
        }
        return false;
    }
}
