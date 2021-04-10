package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.Vec2d;
import malte0811.controlengineering.util.Vec2i;

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

    public void render(MatrixStack transform) {
        symbol.render(transform, pos.x, pos.y);
    }

    public SymbolInstance<?> getSymbol() {
        return symbol;
    }

    public Vec2i getPosition() {
        return pos;
    }

    public boolean canCoexist(PlacedSymbol other) {
        return (pos.x + symbol.getXSize() < other.pos.x || other.pos.x + other.getSymbol().getXSize() < pos.x) ||
                (pos.y + symbol.getYSize() < other.pos.y || other.pos.y + other.getSymbol().getYSize() < pos.y);
    }

    public boolean containsPoint(Vec2d p) {
        return p.x >= pos.x && p.x <= pos.x + symbol.getXSize() && p.y >= pos.y && p.y <= pos.y + symbol.getYSize();
    }
}
