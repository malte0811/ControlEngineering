package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.schematic.symbol.CellSymbol;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class Schematic {
    public static final int GLOBAL_MIN = -512;
    public static final int GLOBAL_MAX = 512;
    public static final RectangleI BOUNDARY = new RectangleI(GLOBAL_MIN, GLOBAL_MIN, GLOBAL_MAX, GLOBAL_MAX);

    public static final Codec<Schematic> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.list(PlacedSymbol.CODEC).fieldOf("symbols").forGetter(s -> s.symbols),
                    Codec.list(SchematicNet.CODEC).fieldOf("nets").forGetter(s -> s.nets)
            ).apply(inst, Schematic::new)
    );

    private final List<PlacedSymbol> symbols;
    private final List<SchematicNet> nets;
    private final SchematicChecker checker;

    private Schematic(List<PlacedSymbol> symbols, List<SchematicNet> nets) {
        this.symbols = new ArrayList<>(symbols);
        this.nets = new ArrayList<>(nets);
        this.resetConnectedPins();
        this.checker = new SchematicChecker(this);
    }

    public Schematic() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    public void addSymbol(PlacedSymbol newSymbol) {
        symbols.add(newSymbol);
        resetConnectedPins();
    }

    public void addWire(WireSegment segment) {
        IntSet connectedIndices = getConnectedNetIndices(segment);
        if (connectedIndices.isEmpty()) {
            // New net
            nets.add(new SchematicNet(segment));
        } else if (connectedIndices.size() == 1) {
            // Within one net
            final int netId = connectedIndices.iterator().nextInt();
            nets.get(netId).addSegment(segment);
        } else {
            // connecting two nets
            Preconditions.checkState(connectedIndices.size() == 2);
            IntIterator it = connectedIndices.iterator();
            final int netIdToKeep = it.nextInt();
            final int netIdToRemove = it.nextInt();
            final SchematicNet netToKeep = nets.get(netIdToKeep);
            final SchematicNet netToRemove = nets.remove(netIdToRemove);
            netToKeep.addAll(netToRemove);
            netToKeep.addSegment(segment);
        }
        resetConnectedPins();
    }

    public boolean removeOneContaining(Vec2d mouse) {
        for (Iterator<SchematicNet> iterator = nets.iterator(); iterator.hasNext(); ) {
            SchematicNet net = iterator.next();
            if (net.removeOneContaining(mouse.floor())) {
                iterator.remove();
                nets.addAll(net.splitComponents());
                resetConnectedPins();
                return true;
            }
        }
        for (Iterator<PlacedSymbol> iterator = symbols.iterator(); iterator.hasNext(); ) {
            if (iterator.next().containsPoint(mouse)) {
                iterator.remove();
                resetConnectedPins();
                return true;
            }
        }
        return false;
    }

    public IntSet getConnectedNetIndices(WireSegment toAdd) {
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

    public void render(PoseStack stack, Vec2d mouse) {
        for (PlacedSymbol s : symbols) {
            s.render(stack);
        }
        for (SchematicNet net : nets) {
            net.render(stack, mouse, symbols);
        }
    }

    @Nullable
    public PlacedSymbol getSymbolAt(Vec2d pos) {
        for (PlacedSymbol symbol : symbols) {
            if (symbol.containsPoint(pos)) {
                return symbol;
            }
        }
        return null;
    }

    private void resetConnectedPins() {
        nets.forEach(SchematicNet::resetCachedPins);
    }

    public List<PlacedSymbol> getSymbols() {
        return Collections.unmodifiableList(symbols);
    }

    public List<SchematicNet> getNets() {
        return Collections.unmodifiableList(nets);
    }

    public SchematicChecker getChecker() {
        return checker;
    }

    private int getTotalCost(ToDoubleFunction<CellCost> individualCost) {
        double sum = 0.0;
        for (PlacedSymbol placedSymbol : getSymbols()) {
            SchematicSymbol<?> s = placedSymbol.getSymbol().getType();
            if (s instanceof CellSymbol cellSymbol) {
                CellCost cost = cellSymbol.getCellType().getCost();
                sum += individualCost.applyAsDouble(cost);
            }
        }
        return Mth.ceil(sum);
    }

    public int getNumTubes() {
        return getTotalCost(CellCost::numTubes);
    }

    public int getWireLength() {
        return getTotalCost(CellCost::wireLength);
    }

    public int getSolderAmount() {
        return getTotalCost(CellCost::getSolderAmount);
    }
}
