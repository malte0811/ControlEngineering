package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.StringJoiner;

public class PlacedSymbol {
    public static final Codec<PlacedSymbol> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Vec2i.CODEC.fieldOf("pos").forGetter(PlacedSymbol::getPosition),
                    SymbolInstance.CODEC.fieldOf("symbol").forGetter(PlacedSymbol::getSymbol)
            ).apply(inst, PlacedSymbol::new)
    );

    private final Vec2i pos;
    private final SymbolInstance<?> symbol;

    public PlacedSymbol(Vec2i pos, SymbolInstance<?> symbol) {
        this.pos = pos;
        this.symbol = symbol;
    }

    public void render(PoseStack transform) {
        symbol.render(transform, pos.x, pos.y);
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
