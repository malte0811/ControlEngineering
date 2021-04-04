package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;

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
    }

    public SymbolInstance<?> getSymbol() {
        return symbol;
    }

    public boolean canCoexist(PlacedSymbol other) {
        return (posX + symbol.getXSize() < other.posX || other.posX + other.getSymbol().getXSize() < posX) ||
                (posY + symbol.getYSize() < other.posY || other.posY + other.getSymbol().getYSize() < posY);
    }
}
