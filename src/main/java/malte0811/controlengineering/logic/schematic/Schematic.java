package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class Schematic {
    private final List<PlacedSymbol> symbols = new ArrayList<>();

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
        // TODO perform splits? How do I represent wire intersections/blobs?
    }

    public boolean canPlace(PlacedSymbol candidate) {
        return symbols.stream().allMatch(candidate::canCoexist);
    }

    public void render(MatrixStack stack) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
    }
}
