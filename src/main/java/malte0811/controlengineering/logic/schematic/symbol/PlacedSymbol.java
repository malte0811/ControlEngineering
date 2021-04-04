package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import java.util.List;

public class PlacedSymbol {
    private final int posX;
    private final int posY;
    private final SymbolInstance<?> symbol;

    public PlacedSymbol(int posX, int posY, SymbolInstance<?> symbol) {
        this.posX = posX;
        this.posY = posY;
        this.symbol = symbol;
    }

    public void render(MatrixStack transform) {
        symbol.render(transform, posX, posY);
        //TODO remove or keep in some form?
        drawPins(transform, symbol.getType().getInputPins(), 0xff00ff00);
        drawPins(transform, symbol.getType().getOutputPins(), 0xffff0000);
    }

    public SymbolInstance<?> getSymbol() {
        return symbol;
    }

    public boolean canCoexist(PlacedSymbol other) {
        return (posX + symbol.getXSize() < other.posX || other.posX + other.getSymbol().getXSize() < posX) ||
                (posY + symbol.getYSize() < other.posY || other.posY + other.getSymbol().getYSize() < posY);
    }

    public boolean containsPoint(double x, double y) {
        return x >= posX && x <= posX + symbol.getXSize() && y >= posY && y <= posY + symbol.getYSize();
    }

    private void drawPins(MatrixStack stack, List<SymbolPin> pinPositions, int color) {
        for (SymbolPin pin : pinPositions) {
            final Vec2i pos = pin.getPosition();
            AbstractGui.fill(stack, posX + pos.x, posY + pos.y, posX + pos.x + 1, posY + pos.y + 1, color);
        }
    }
}
