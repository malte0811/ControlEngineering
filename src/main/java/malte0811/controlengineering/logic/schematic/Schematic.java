package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.*;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class Schematic {
    public static final int GLOBAL_MIN = -512;
    public static final int GLOBAL_MAX = 512;
    public static final RectangleI BOUNDARY = new RectangleI(GLOBAL_MIN, GLOBAL_MIN, GLOBAL_MAX, GLOBAL_MAX);

    public static final MyCodec<Schematic> CODEC = new RecordCodec3<>(
            new CodecField<>("symbols", Schematic::getSymbols, MyCodecs.list(PlacedSymbol.CODEC)),
            new CodecField<>("nets", Schematic::getNets, MyCodecs.list(SchematicNet.CODEC)),
            new CodecField<>("name", Schematic::getName, MyCodecs.STRING),
            Schematic::new
    );

    private final List<PlacedSymbol> symbols;
    private final List<SchematicNet> nets;
    private String name;

    private Schematic(List<PlacedSymbol> symbols, List<SchematicNet> nets, String name) {
        this.symbols = new ArrayList<>(symbols);
        this.nets = new ArrayList<>(nets);
        this.name = name;
        this.resetConnectedPins();
    }

    public Schematic() {
        this(ImmutableList.of(), ImmutableList.of(), "New schematic");
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
        } else {
            // Connected to existing net(s)
            IntIterator it = connectedIndices.iterator();
            final int netIdToKeep = it.nextInt();
            final SchematicNet netToKeep = nets.get(netIdToKeep);
            while (it.hasNext()) {
                final SchematicNet netToRemove = nets.remove(it.nextInt());
                netToKeep.addAll(netToRemove);
            }
            netToKeep.addSegment(segment);
        }
        resetConnectedPins();
    }

    public boolean removeOneContaining(Vec2d mouse, Level level) {
        for (int i = 0; i < nets.size(); ++i) {
            SchematicNet net = nets.get(i);
            if (net.removeOneContaining(mouse.floor())) {
                nets.remove(i);
                nets.addAll(net.splitComponents());
                resetConnectedPins();
                return true;
            }
        }
        for (int i = 0; i < symbols.size(); ++i) {
            if (symbols.get(i).containsPoint(mouse, level)) {
                removeSymbol(i);
                return true;
            }
        }
        return false;
    }

    public List<SegmentsInNet> getWiresWithin(RectangleI area) {
        List<SegmentsInNet> contained = new ArrayList<>();
        for (int net = 0; net < nets.size(); net++) {
            IntList containedNetSegments = new IntArrayList();
            List<WireSegment> segments = nets.get(net).getAllSegments();
            for (int segment = 0; segment < segments.size(); segment++) {
                if (area.contains(segments.get(segment).getShape())) {
                    containedNetSegments.add(segment);
                }
            }
            if (!containedNetSegments.isEmpty()) {
                contained.add(new SegmentsInNet(net, containedNetSegments));
            }
        }
        return contained;
    }

    public IntList getSymbolIndicesWithin(RectangleI area, Level level) {
        IntList contained = new IntArrayList();
        for (int i = 0; i < symbols.size(); i++) {
            if (area.contains(symbols.get(i).getShape(level))) {
                contained.add(i);
            }
        }
        return contained;
    }

    public void removeSymbol(int index) {
        symbols.remove(index);
        resetConnectedPins();
    }

    public void removeSegments(SegmentsInNet index) {
        final var oldNet = nets.remove(index.netIdx());
        oldNet.removeSegments(index.segments());
        nets.addAll(oldNet.splitComponents());
        resetConnectedPins();
    }

    private boolean isConnected(SchematicNet net, WireSegment wire) {
        // Added segment ends inside the net
        for (Vec2i end : wire.getEnds()) {
            if (net.contains(end)) {
                return true;
            }
        }
        // Some net segment ends inside the added segment
        for (var netWire : net.getAllSegments()) {
            for (var netEnd : netWire.getEnds()) {
                if (wire.containsClosed(netEnd)) {
                    return true;
                }
            }
        }
        // Net and added segment have a pin in common (possibly internal to segments in both cases)
        for (var pin : net.getOrComputePins(getSymbols())) {
            if (wire.containsClosed(pin.getPosition())) {
                return true;
            }
        }
        return false;
    }

    public IntSet getConnectedNetIndices(WireSegment toAdd) {
        IntSet indices = new IntArraySet();
        for (int i = 0; i < nets.size(); ++i) {
            if (isConnected(nets.get(i), toAdd)) {
                indices.add(i);
            }
        }
        return indices;
    }

    @Nullable
    public PlacedSymbol getSymbolAt(Vec2d pos, Level level) {
        for (PlacedSymbol symbol : symbols) {
            if (symbol.containsPoint(pos, level)) {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SchematicChecker makeChecker(Level level) {
        return new SchematicChecker(this, level);
    }

    private int getTotalCost(ToDoubleFunction<CellCost> individualCost) {
        return (int) Math.ceil(
                getSymbols().stream()
                        .map(PlacedSymbol::symbol)
                        .map(SymbolInstance::getType)
                        .map(SchematicSymbol::getCost)
                        .mapToDouble(individualCost)
                        .sum()
        );
    }

    public int getNumLogicTubes() {
        return getTotalCost(CellCost::numTubes);
    }

    public int getWireLength() {
        return getTotalCost(CellCost::wireLength);
    }

    public int getSolderAmount() {
        return getTotalCost(CellCost::getSolderAmount);
    }

    public int getNumBoards() {
        return LogicCabinetBlockEntity.getNumBoardsFor(getNumLogicTubes());
    }

    public void clear() {
        symbols.clear();
        nets.clear();
    }

    public boolean replaceBy(int index, PlacedSymbol newSymbol, Level level) {
        final var oldSymbol = symbols.remove(index);
        resetConnectedPins();
        if (makeChecker(level).canAdd(newSymbol)) {
            addSymbol(newSymbol);
            return true;
        } else {
            // Add back old symbol
            addSymbol(oldSymbol);
            return false;
        }
    }

    public boolean isEmpty() {
        return symbols.isEmpty() && nets.isEmpty();
    }

    public static boolean isEmpty(@Nullable Schematic schematic) {
        return schematic == null || schematic.isEmpty();
    }

    public Schematic copy() {
        final var newSymbols = symbols.stream().map(PlacedSymbol::copy).toList();
        final var newNets = nets.stream().map(SchematicNet::copy).toList();
        return new Schematic(newSymbols, newNets, name);
    }

    public List<WireSegment> getWireSegments(SegmentsInNet segments) {
        return nets.get(segments.netIdx).getSegments(segments.segments);
    }

    public record SegmentsInNet(int netIdx, IntList segments) {
        public static final MyCodec<SegmentsInNet> CODEC = new RecordCodec2<>(
                MyCodecs.INTEGER.fieldOf("netIdx", SegmentsInNet::netIdx),
                MyCodecs.INT_LIST.fieldOf("segments", SegmentsInNet::segments),
                SegmentsInNet::new
        );
    }
}
