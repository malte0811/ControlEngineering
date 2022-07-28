package malte0811.controlengineering.logic.schematic.symbol;

import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.world.level.Level;

public record PlacedSymbol(Vec2i position, SymbolInstance<?> symbol) {
    public static final MyCodec<PlacedSymbol> CODEC = new RecordCodec2<>(
            new CodecField<>("pos", PlacedSymbol::position, Vec2i.CODEC),
            new CodecField<>("symbol", PlacedSymbol::symbol, SymbolInstance.CODEC),
            PlacedSymbol::new
    );

    public RectangleI getShape(Level level) {
        return new RectangleI(position(), getMaxPoint(level));
    }

    public boolean canCoexist(PlacedSymbol other, Level level) {
        return getShape(level).disjoint(other.getShape(level));
    }

    public boolean containsPoint(Vec2d p, Level level) {
        return getShape(level).containsClosed(p);
    }

    public boolean isInRectangle(RectangleI rectangle) {
        return rectangle.containsClosed(position);
    }

    public Vec2i getMaxPoint(Level level) {
        return position.add(new Vec2i(symbol().getXSize(level), symbol().getYSize(level)));
    }
}
