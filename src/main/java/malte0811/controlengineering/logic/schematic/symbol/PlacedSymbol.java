package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import java.util.List;

public class PlacedSymbol {
    private final Vec2i pos;
    private final SymbolInstance<?> symbol;

    public PlacedSymbol(int posX, int posY, SymbolInstance<?> symbol) {
        this(new Vec2i(posX, posY), symbol);
    }

    public PlacedSymbol(Vec2i pos, SymbolInstance<?> symbol) {
        this.pos = pos;
        this.symbol = symbol;
    }

    public void render(MatrixStack transform) {
        symbol.render(transform, pos.x, pos.y);
        //TODO remove or keep in some form?
        drawPins(transform, symbol.getType().getInputPins(), 0xff00ff00);
        drawPins(transform, symbol.getType().getOutputPins(), 0xffff0000);
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

    public boolean containsPoint(double x, double y) {
        return x >= pos.x && x <= pos.x + symbol.getXSize() && y >= pos.y && y <= pos.y + symbol.getYSize();
    }

    private void drawPins(MatrixStack stack, List<SymbolPin> pinPositions, int color) {
        for (SymbolPin pin : pinPositions) {
            final Vec2i pinPos = pin.getPosition();
            AbstractGui.fill(
                    stack, pos.x + pinPos.x, pos.y + pinPos.y, pos.x + pinPos.x + 1, pos.y + pinPos.y + 1, color
            );
        }
    }
}
