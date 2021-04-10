package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.Vec2d;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SchematicNet {
    public static final int WIRE_COLOR = 0xff_f0_aa_2a;
    public static final int SELECTED_WIRE_COLOR = 0xff_f0_fa_2a;
    public static final Codec<SchematicNet> CODEC = Codec.list(WireSegment.CODEC)
            .xmap(SchematicNet::new, s -> s.segments);

    private final List<WireSegment> segments;
    @Nullable
    private Set<ConnectedPin> pins;

    public SchematicNet() {
        this(ImmutableList.of());
    }

    public SchematicNet(List<WireSegment> wires) {
        this.segments = new ArrayList<>(wires);
    }

    public void addSegment(WireSegment segment) {
        // TODO simplify segments
        List<WireSegment> newSegments = new ArrayList<>();
        for (Iterator<WireSegment> iterator = segments.iterator(); iterator.hasNext(); ) {
            WireSegment existing = iterator.next();
            for (Vec2i end : segment.getEnds()) {
                if (existing.containsOpen(end)) {
                    newSegments.addAll(existing.splitAt(end));
                    iterator.remove();
                    break;
                }
            }
        }
        newSegments.add(segment);
        segments.addAll(newSegments);
    }

    public void addAll(SchematicNet other) {
        segments.addAll(other.segments);
    }

    public boolean contains(Vec2i point) {
        return segments.stream().anyMatch(s -> s.containsClosed(point));
    }

    public void render(MatrixStack stack, Vec2d mouse, List<PlacedSymbol> symbols) {
        final int color = contains(mouse.floor()) ? SELECTED_WIRE_COLOR : WIRE_COLOR;
        for (WireSegment segment : segments) {
            segment.renderWithoutBlobs(stack, color);
        }
        Object2IntOpenHashMap<Vec2i> endsAt = new Object2IntOpenHashMap<>();
        for (WireSegment segment : segments) {
            for (Vec2i end : segment.getEnds()) {
                if (endsAt.addTo(end, 1) == 2) {
                    AbstractGui.fill(stack, end.x, end.y, end.x + 1, end.y + 1, color);
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

    public boolean canMerge(SchematicNet other, List<PlacedSymbol> symbols) {
        Set<ConnectedPin> totalPins = new HashSet<>(getOrComputePins(symbols));
        totalPins.addAll(other.getOrComputePins(symbols));
        return ConnectedPin.isConsistent(totalPins);
    }

    public boolean canAdd(WireSegment segment, List<PlacedSymbol> symbols) {
        return canMerge(new SchematicNet(ImmutableList.of(segment)), symbols);
    }

    private boolean containsPin(PlacedSymbol symbol, SymbolPin pin) {
        final Vec2i actualPinPos = pin.getPosition().add(symbol.getPosition());
        return contains(actualPinPos);
    }

    public boolean removeOneContaining(Vec2i point) {
        for (Iterator<WireSegment> iterator = segments.iterator(); iterator.hasNext(); ) {
            if (iterator.next().containsClosed(point)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public List<SchematicNet> splitComponents() {
        Map<Vec2i, SchematicNet> indexForPoint = new HashMap<>();
        for (WireSegment segment : segments) {
            final SchematicNet first = indexForPoint.get(segment.getStart());
            final SchematicNet second = indexForPoint.get(segment.getEnd());
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
}
