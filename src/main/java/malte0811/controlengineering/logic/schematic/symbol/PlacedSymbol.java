package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;

public record PlacedSymbol(Vec2i position, SymbolInstance<?> symbol) {
    public static final MyCodec<PlacedSymbol> CODEC = new RecordCodec2<>(
            new CodecField<>("pos", PlacedSymbol::position, Vec2i.CODEC),
            new CodecField<>("symbol", PlacedSymbol::symbol, SymbolInstance.CODEC),
            PlacedSymbol::new
    );

    public void render(PoseStack transform) {
        symbol.render(transform, position.x(), position.y());
    }

    public RectangleI getShape() {
        return new RectangleI(position(), getMaxPoint());
    }

    public boolean canCoexist(PlacedSymbol other) {
        return getShape().disjoint(other.getShape());
    }

    public boolean containsPoint(Vec2d p) {
        return getShape().containsClosed(p);
    }

    public Vec2i getMaxPoint() {
        return position.add(new Vec2i(symbol().getXSize(), symbol().getYSize()));
    }
}
