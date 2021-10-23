package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static malte0811.controlengineering.logic.schematic.WireSegment.WireAxis.X;

public class SchematicNet {
    public static final int WIRE_COLOR = 0xff_f0_aa_2a;
    public static final int SELECTED_WIRE_COLOR = 0xff_f0_fa_2a;
    private static final Codec<List<WireSegment>> WIRE_LIST_CODEC = Codec.list(WireSegment.CODEC);
    public static final Codec<SchematicNet> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    WIRE_LIST_CODEC.fieldOf("horizontal").forGetter(n -> n.horizontalSegments),
                    WIRE_LIST_CODEC.fieldOf("vertical").forGetter(n -> n.verticalSegments)
            ).apply(inst, SchematicNet::new)
    );

    private final List<WireSegment> horizontalSegments;
    private final List<WireSegment> verticalSegments;
    private final Iterable<WireSegment> allSegments;
    @Nullable
    private Set<ConnectedPin> pins;

    public SchematicNet() {
        this(ImmutableList.of(), ImmutableList.of());
    }

    public SchematicNet(WireSegment singleWire) {
        this(ImmutableList.of(), ImmutableList.of());
        addSegment(singleWire);
    }

    public SchematicNet(List<WireSegment> horizontal, List<WireSegment> vertical) {
        this.horizontalSegments = new ArrayList<>(horizontal);
        this.verticalSegments = new ArrayList<>(vertical);
        this.allSegments = () -> Iterators.concat(horizontalSegments.iterator(), verticalSegments.iterator());
    }

    public void addSegment(WireSegment segment) {
        if (segment.axis() == X) {
            horizontalSegments.add(segment);
        } else {
            verticalSegments.add(segment);
        }
        simplify();
    }

    public void addAll(SchematicNet other) {
        verticalSegments.addAll(other.verticalSegments);
        horizontalSegments.addAll(other.horizontalSegments);
        simplify();
    }

    public boolean contains(Vec2i point) {
        for (WireSegment s : allSegments) {
            if (s.containsClosed(point)) {
                return true;
            }
        }
        return false;
    }

    public void render(PoseStack stack, Vec2d mouse, List<PlacedSymbol> symbols) {
        final int color = contains(mouse.floor()) ? SELECTED_WIRE_COLOR : WIRE_COLOR;
        for (WireSegment segment : allSegments) {
            segment.renderWithoutBlobs(stack, color);
        }
        Object2IntOpenHashMap<Vec2i> endsAt = new Object2IntOpenHashMap<>();
        for (WireSegment segment : allSegments) {
            for (Vec2i end : segment.getEnds()) {
                if (endsAt.addTo(end, 1) == 2) {
                    GuiComponent.fill(stack, end.x(), end.y(), end.x() + 1, end.y() + 1, color);
                }
            }
        }
        for (ConnectedPin pin : getOrComputePins(symbols)) {
            pin.render(stack, color);
        }
    }

    public Set<ConnectedPin> computeConnectedPins(List<PlacedSymbol> symbols) {
        Set<ConnectedPin> connected = new HashSet<>();
        for (PlacedSymbol s : symbols) {
            for (SymbolPin output : s.getSymbol().getPins()) {
                if (containsPin(s, output)) {
                    connected.add(new ConnectedPin(s, output));
                }
            }
        }
        return Collections.unmodifiableSet(connected);
    }

    public Set<ConnectedPin> getOrComputePins(List<PlacedSymbol> symbols) {
        if (pins == null) {
            pins = computeConnectedPins(symbols);
        }
        return pins;
    }

    public void resetCachedPins() {
        pins = null;
    }

    public Optional<Component> canMerge(SchematicNet other, List<PlacedSymbol> symbols) {
        Set<ConnectedPin> totalPins = new HashSet<>(getOrComputePins(symbols));
        totalPins.addAll(other.getOrComputePins(symbols));
        return SchematicChecker.getConsistencyError(totalPins);
    }

    public boolean canAdd(WireSegment segment, List<PlacedSymbol> symbols) {
        return !canMerge(new SchematicNet(segment), symbols).isPresent();
    }

    private boolean containsPin(PlacedSymbol symbol, SymbolPin pin) {
        final Vec2i actualPinPos = pin.position().add(symbol.getPosition());
        return contains(actualPinPos);
    }

    public boolean removeOneContaining(Vec2i point) {
        for (Iterator<WireSegment> iterator = allSegments.iterator(); iterator.hasNext(); ) {
            if (iterator.next().containsClosed(point)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public List<SchematicNet> splitComponents() {
        // TODO improve using horizontal/vertical structure?
        Map<Vec2i, SchematicNet> indexForPoint = new HashMap<>();
        for (WireSegment segment : allSegments) {
            final SchematicNet first = indexForPoint.get(segment.start());
            final SchematicNet second = indexForPoint.get(segment.end());
            final SchematicNet finalNet;
            if (first == null && second == null) {
                // New net
                finalNet = new SchematicNet();
            } else if (first == null || second == null || first == second) {
                finalNet = first != null ? first : second;
            } else {
                finalNet = first;
                for (WireSegment segmentToMove : second.allSegments) {
                    first.addSegment(segmentToMove);
                    for (Vec2i end : segmentToMove.getEnds()) {
                        indexForPoint.put(end, finalNet);
                    }
                }
            }
            finalNet.addSegment(segment);
            for (Vec2i end : segment.getEnds()) {
                indexForPoint.put(end, finalNet);
            }
        }
        return indexForPoint.values()
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void simplify() {
        mergeIntervals();
        splitIntersections();
    }

    private void mergeIntervals() {
        mergeIntervalsIn(X, horizontalSegments);
        mergeIntervalsIn(WireSegment.WireAxis.Y, verticalSegments);
    }

    private static void mergeIntervalsIn(WireSegment.WireAxis axis, List<WireSegment> segments) {
        segments.sort(
                Comparator.<WireSegment>comparingInt(s -> axis.other().get(s.start()))
                        .thenComparing(s -> axis.get(s.start()))
        );
        for (int i = 1; i < segments.size(); i++) {
            final WireSegment last = segments.get(i - 1);
            final WireSegment current = segments.get(i);
            if (last.isOnExtendedWire(current.start())) {
                final int lastEnd = axis.get(last.end());
                final int currentStart = axis.get(current.start());
                if (lastEnd >= currentStart) {
                    final int addedLength = Math.max(0, axis.get(current.end()) - lastEnd);
                    segments.set(i - 1, new WireSegment(last.start(), last.length() + addedLength, axis));
                    segments.remove(i);
                    --i;
                }
            }
        }
    }

    private void splitIntersections() {
        // TODO fancy sweepline approach?
        for (int xI = 0; xI < horizontalSegments.size(); xI++) {
            final WireSegment horizontal = horizontalSegments.get(xI);
            for (int yI = 0; yI < verticalSegments.size(); yI++) {
                final WireSegment vertical = verticalSegments.get(yI);
                if (horizontal.crossesOneOpen(vertical)) {
                    final Vec2i intersection = new Vec2i(vertical.start().x(), horizontal.start().y());
                    if (vertical.containsOpen(intersection)) {
                        verticalSegments.remove(yI);
                        --yI;
                        verticalSegments.addAll(vertical.splitAt(intersection));
                    }
                    if (horizontal.containsOpen(intersection)) {
                        horizontalSegments.remove(xI);
                        --xI;
                        horizontalSegments.addAll(horizontal.splitAt(intersection));
                        break;
                    }
                }
            }
        }
    }
}
