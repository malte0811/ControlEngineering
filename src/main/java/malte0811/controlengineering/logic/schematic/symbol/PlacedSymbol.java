package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;

import java.util.StringJoiner;

public record PlacedSymbol(Vec2i pos, SymbolInstance<?> symbol) {
    public static final MyCodec<PlacedSymbol> CODEC = new RecordCodec2<>(
            new CodecField<>("pos", PlacedSymbol::pos, Vec2i.CODEC),
            new CodecField<>("symbol", PlacedSymbol::symbol, SymbolInstance.CODEC),
            PlacedSymbol::new
    );

    public void render(PoseStack transform) {
        symbol.render(transform, pos.x(), pos.y());
    }

    public SymbolInstance<?> getSymbol() {
        return symbol;
    }

    public Vec2i getPosition() {
        return pos;
    }

    public RectangleI getShape() {
        return new RectangleI(getPosition(), getMaxPoint());
    }

    public boolean canCoexist(PlacedSymbol other) {
        return getShape().disjoint(other.getShape());
    }

    public boolean containsPoint(Vec2d p) {
        return getShape().containsClosed(p);
    }

    public Vec2i getMaxPoint() {
        return pos.add(new Vec2i(getSymbol().getXSize(), getSymbol().getYSize()));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PlacedSymbol.class.getSimpleName() + "[", "]")
                .add("pos=" + pos)
                .add("symbol=" + symbol)
                .toString();
    }
}
