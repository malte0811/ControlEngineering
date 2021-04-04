package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.util.Vec2i;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Schematic {
    private final List<PlacedSymbol> symbols = new ArrayList<>();
    private final List<SchematicNet> nets = new ArrayList<>();

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
    }

    public boolean canPlace(PlacedSymbol candidate) {
        if (!symbols.stream().allMatch(candidate::canCoexist)) {
            // Intersects with other symbol(s)
            return false;
        }
        for (SchematicNet net : nets) {
            Set<SchematicNet.ConnectedPin> pinsInNet = net.getConnectedPins(symbols);
            pinsInNet.addAll(net.getConnectedPins(Collections.singletonList(candidate)));
            if (!SchematicNet.ConnectedPin.isConsistent(pinsInNet)) {
                // Would make a net inconsistent, i.e. would add multiple sources or analog/digital incompatibility
                return false;
            }
        }
        return true;
    }

    public void addWire(WireSegment segment) {
        IntSet connectedIndices = getConnectedIndices(segment);
        if (connectedIndices.isEmpty()) {
            // New net
            nets.add(new SchematicNet(ImmutableList.of(segment)));
        } else if (connectedIndices.size() == 1) {
            // Within one net
            nets.get(connectedIndices.iterator().nextInt()).addSegment(segment);
        } else {
            // connecting two nets
            Preconditions.checkState(connectedIndices.size() == 2);
            IntIterator it = connectedIndices.iterator();
            final SchematicNet netToKeep = nets.get(it.nextInt());
            final SchematicNet netToRemove = nets.remove(it.nextInt());
            netToKeep.addAll(netToRemove);
            netToKeep.addSegment(segment);
        }
    }

    public boolean canAdd(WireSegment segment) {
        Set<SchematicNet.ConnectedPin> pinsOnWire = new SchematicNet(
                Collections.singletonList(segment)
        ).getConnectedPins(symbols);
        if (!SchematicNet.ConnectedPin.isConsistent(pinsOnWire)) {
            return false;
        }
        IntSet netsToCheck = getConnectedIndices(segment);
        for (int netIdA : netsToCheck) {
            SchematicNet netA = nets.get(netIdA);
            for (int netIdB : netsToCheck) {
                if (netIdB > netIdA) {
                    SchematicNet netB = nets.get(netIdA);
                    if (!netA.canMerge(netB, symbols)) {
                        return false;
                    }
                }
            }
            if (!netA.canAdd(segment, symbols)) {
                return false;
            }
        }
        return true;
    }

    private IntSet getConnectedIndices(WireSegment toAdd) {
        IntSet indices = new IntArraySet();
        for (int i = 0; i < nets.size(); ++i) {
            final SchematicNet net = nets.get(i);
            for (Vec2i end : toAdd.getEnds()) {
                if (net.contains(end)) {
                    indices.add(i);
                }
            }
        }
        return indices;
    }

    public void render(MatrixStack stack) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
        // TODO highlight nets under cursor
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
