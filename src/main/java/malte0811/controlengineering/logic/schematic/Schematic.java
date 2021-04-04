package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Schematic {
    private final List<PlacedSymbol> symbols = new ArrayList<>();
    private final List<SchematicNet> nets = new ArrayList<>();

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
    }

    public boolean canPlace(PlacedSymbol candidate) {
        return symbols.stream().allMatch(candidate::canCoexist);
    }

    public void addWire(WireSegment segment) {
        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < nets.size() && (startIndex < 0 || endIndex < 0); ++i) {
            final SchematicNet net = nets.get(i);
            if (net.contains(segment.getStart())) {
                startIndex = i;
            }
            if (net.contains(segment.getEnd())) {
                endIndex = i;
            }
        }
        if (startIndex < 0 && endIndex < 0) {
            // New net
            nets.add(new SchematicNet(ImmutableList.of(segment)));
        } else if (startIndex == endIndex || startIndex < 0 || endIndex < 0) {
            // Within one net
            nets.get(Math.max(startIndex, endIndex)).addSegment(segment);
        } else {
            // connecting two nets
            final SchematicNet netToKeep = nets.get(Math.min(startIndex, endIndex));
            final SchematicNet netToRemove = nets.remove(Math.max(startIndex, endIndex));
            netToKeep.addAll(netToRemove);
            netToKeep.addSegment(segment);
        }
    }

    public void render(MatrixStack stack) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
        for (SchematicNet net : nets) {
            net.render(stack);
        }
    }

    @Nullable
    public PlacedSymbol getSymbolAt(double actualMouseX, double actualMouseY) {
        for (PlacedSymbol symbol : symbols) {
            if (symbol.containsPoint(actualMouseX, actualMouseY)) {
                return symbol;
            }
        }
        return null;
    }
}
