package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import net.minecraft.client.gui.GuiComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static malte0811.controlengineering.logic.schematic.WireSegment.WireAxis.X;
import static malte0811.controlengineering.logic.schematic.WireSegment.WireAxis.Y;

public class SchematicNet {
    public static final int MOVING_WIRE_COLOR = 0x80_f0_aa_2a;
    public static final int WIRE_COLOR = 0xff_f0_aa_2a;
    public static final int SELECTED_WIRE_COLOR = 0xff_f0_fa_2a;
    private static final MyCodec<List<WireSegment>> WIRE_LIST_CODEC = MyCodecs.list(WireSegment.CODEC);
    public static final MyCodec<SchematicNet> OLD_CODEC = new RecordCodec2<>(
            new CodecField<>("horizontal", n -> { throw new UnsupportedOperationException(); }, WIRE_LIST_CODEC),
            new CodecField<>("vertical", n -> { throw new UnsupportedOperationException(); }, WIRE_LIST_CODEC),
            (l1, l2) -> {
                List<WireSegment> merged = new ArrayList<>();
                merged.addAll(l1);
                merged.addAll(l2);
                return new SchematicNet(merged);
            }
    );
    public static final MyCodec<SchematicNet> CODEC = WIRE_LIST_CODEC.xmap(
            SchematicNet::new, SchematicNet::getAllSegments
    ).orElse(OLD_CODEC);

    private final List<WireSegment> segments;
    @Nullable
    private Set<ConnectedPin> pins;

    public SchematicNet() {
        this(List.of());
    }

    public SchematicNet(WireSegment singleWire) {
        this(List.of(singleWire));
    }

    public SchematicNet(List<WireSegment> segments) {
        this.segments = new ArrayList<>(segments);
    }

    public void addSegment(WireSegment segment) {
        segments.add(segment);
        simplify();
    }

    public void addAll(SchematicNet other) {
        segments.addAll(other.segments);
        simplify();
    }

    public boolean contains(Vec2i point) {
        for (WireSegment s : segments) {
            if (s.containsClosed(point)) {
                return true;
            }
        }
        return false;
    }

    public void render(PoseStack stack, int color, List<PlacedSymbol> symbols) {
        for (WireSegment segment : segments) {
            segment.renderWithoutBlobs(stack, color);
        }
        Object2IntOpenHashMap<Vec2i> endsAt = new Object2IntOpenHashMap<>();
        for (WireSegment segment : segments) {
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
            for (SymbolPin output : s.symbol().getPins()) {
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

    private boolean containsPin(PlacedSymbol symbol, SymbolPin pin) {
        final Vec2i actualPinPos = pin.position().add(symbol.position());
        return contains(actualPinPos);
    }

    public boolean removeOneContaining(Vec2i point) {
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).containsClosed(point)) {
                removeSegments(IntLists.singleton(i));
                return true;
            }
        }
        return false;
    }

    public void removeSegments(IntList sortedSegments) {
        for (final int index : Lists.reverse(sortedSegments)) {
            segments.remove(index);
        }
        simplify();
    }

    public List<SchematicNet> splitComponents() {
        Map<Vec2i, SchematicNet> indexForPoint = new HashMap<>();
        for (WireSegment segment : segments) {
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
                for (WireSegment segmentToMove : second.segments) {
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

    public List<WireSegment> getAllSegments() {
        return segments;
    }

    public SchematicNet copy() {
        return new SchematicNet(segments);
    }

    private void mergeIntervals() {
        segments.sort(
                Comparator.comparing(WireSegment::axis)
                        .thenComparingInt(s -> s.axis().other().get(s.start()))
                        .thenComparing(s -> s.axis().get(s.start()))
        );
        for (int i = 1; i < segments.size(); i++) {
            final WireSegment last = segments.get(i - 1);
            final WireSegment current = segments.get(i);
            if (last.axis() != current.axis()) { continue; }
            final var axis = last.axis();
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
        xLoop: for (int xI = 0; xI < segments.size(); xI++) {
            final WireSegment horizontal = segments.get(xI);
            if (horizontal.axis() != X) { continue; }
            yLoop: for (int yI = 0; yI < segments.size(); yI++) {
                final WireSegment vertical = segments.get(yI);
                if (vertical.axis() != Y) { continue; }
                if (horizontal.crossesOneOpen(vertical)) {
                    final Vec2i intersection = new Vec2i(vertical.start().x(), horizontal.start().y());
                    if (vertical.containsOpen(intersection)) {
                        splitSegmentAt(yI, intersection);
                        --yI;
                        continue yLoop;
                    }
                    if (horizontal.containsOpen(intersection)) {
                        splitSegmentAt(xI, intersection);
                        --xI;
                        continue xLoop;
                    }
                }
            }
        }
    }

    private void splitSegmentAt(int segmentIdx, Vec2i split) {
        final var toSplit = segments.remove(segmentIdx);
        segments.addAll(toSplit.splitAt(split));
    }

    public List<WireSegment> getSegments(IntList segments) {
        List<WireSegment> result = new ArrayList<>(segments.size());
        for (final var index : segments) {
            result.add(this.segments.get(index));
        }
        return result;
    }
}
