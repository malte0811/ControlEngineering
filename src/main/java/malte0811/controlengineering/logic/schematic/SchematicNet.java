package malte0811.controlengineering.logic.schematic;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import java.util.*;
import java.util.stream.Collectors;

public class SchematicNet {
    public static final int WIRE_COLOR = 0xfff0aa2a;
    public static final Codec<SchematicNet> CODEC = Codec.list(WireSegment.CODEC)
            .xmap(SchematicNet::new, s -> s.segments);

    private final List<WireSegment> segments;

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

    public void render(MatrixStack stack) {
        for (WireSegment segment : segments) {
            segment.renderWithoutBlobs(stack, WIRE_COLOR);
        }
        Object2IntOpenHashMap<Vec2i> endsAt = new Object2IntOpenHashMap<>();
        for (WireSegment segment : segments) {
            for (Vec2i end : segment.getEnds()) {
                if (endsAt.addTo(end, 1) == 2) {
                    AbstractGui.fill(stack, end.x - 1, end.y - 1, end.x + 2, end.y + 2, WIRE_COLOR);
                }
            }
        }
    }

    public Set<ConnectedPin> getConnectedPins(List<PlacedSymbol> symbols) {
        Set<ConnectedPin> connected = new HashSet<>();
        for (PlacedSymbol s : symbols) {
            SchematicSymbol<?> type = s.getSymbol().getType();
            for (SymbolPin output : type.getOutputPins()) {
                if (containsPin(s, output)) {
                    connected.add(new ConnectedPin(s, output, true));
                }
            }
            for (SymbolPin input : type.getInputPins()) {
                if (containsPin(s, input)) {
                    connected.add(new ConnectedPin(s, input, false));
                }
            }
        }
        return connected;
    }

    public boolean canMerge(SchematicNet other, List<PlacedSymbol> symbols) {
        Set<ConnectedPin> totalPins = getConnectedPins(symbols);
        totalPins.addAll(other.getConnectedPins(symbols));
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

    public static class ConnectedPin {
        private final PlacedSymbol symbol;
        private final SymbolPin pin;
        private final boolean isOutput;

        public ConnectedPin(PlacedSymbol symbol, SymbolPin pin, boolean isOutput) {
            this.symbol = symbol;
            this.pin = pin;
            this.isOutput = isOutput;
        }

        public static boolean isConsistent(Set<ConnectedPin> netPins) {
            ConnectedPin sourcePin = null;
            boolean hasAnalogSource = false;
            boolean hasDigitalSink = false;
            int leftmostX = Integer.MAX_VALUE;
            for (ConnectedPin pin : netPins) {
                if (pin.isOutput()) {
                    if (sourcePin != null) {
                        // Only allow one signal source
                        return false;
                    }
                    sourcePin = pin;
                    if (pin.isAnalog()) {
                        hasAnalogSource = true;
                    }
                } else if (!pin.isAnalog()) {
                    hasDigitalSink = true;
                }
                if (leftmostX > pin.getPosition().x) {
                    leftmostX = pin.getPosition().x;
                }
            }
            if (sourcePin != null && sourcePin.getPosition().x > leftmostX) {
                // there are pins left of the source pin
                return false;
            }
            // Do not allow analog source with digital sink
            return !(hasAnalogSource && hasDigitalSink);
        }

        public boolean isOutput() {
            return isOutput;
        }

        public boolean isAnalog() {
            return pin.getType() == SignalType.ANALOG;
        }

        public SymbolPin getPin() {
            return pin;
        }

        public PlacedSymbol getSymbol() {
            return symbol;
        }

        public Vec2i getPosition() {
            return symbol.getPosition().add(pin.getPosition());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConnectedPin that = (ConnectedPin) o;
            return isOutput == that.isOutput && symbol.equals(that.symbol) && pin.equals(that.pin);
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbol, pin, isOutput);
        }
    }
}
