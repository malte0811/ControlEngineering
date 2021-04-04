package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SchematicNet {
    private static final int WIRE_COLOR = 0xfff0aa2a;

    private final List<WireSegment> segments;

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
}
