package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.util.ScreenUtils;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;

import java.util.List;

public record WireSegment(Vec2i start, int length, WireAxis axis) {
    public static final MyCodec<WireSegment> CODEC = new RecordCodec3<>(
            new CodecField<>("start", WireSegment::start, Vec2i.CODEC),
            new CodecField<>("length", WireSegment::length, MyCodecs.INTEGER),
            new CodecField<>("axis", WireSegment::axis, WireAxis.CODEC),
            WireSegment::new
    );
    public static final float WIRE_SPACE = 1 / 8f;

    public Vec2i end() {
        return axis.addToCoord(start, length);
    }

    public boolean containsClosed(Vec2i point) {
        if (!isOnExtendedWire(point)) {
            return false;
        }
        return containsClosedOnAxis(axis.get(point));
    }

    public boolean containsOpen(Vec2i point) {
        if (!isOnExtendedWire(point)) {
            return false;
        }
        return containsOpenOnAxis(axis.get(point));
    }

    private boolean containsOpenOnAxis(int axisCoord) {
        final int min = axis.get(start);
        return axisCoord > min && axisCoord < min + length;
    }

    private boolean containsClosedOnAxis(int axisCoord) {
        final int min = axis.get(start);
        return axisCoord >= min && axisCoord <= min + length;
    }

    /**
     * Crossing check for splitting. True if the wires are orthogonal to each other, intersect as closed lines and the
     * intersection is in the interior of at least one of the segments
     */
    public boolean crossesOneOpen(WireSegment other) {
        if (axis() == other.axis()) {
            return false;
        } else if (axis() == WireAxis.Y) {
            return other.crossesOneOpen(this);
        }
        // This: X wire, other: Y wire
        final int intersectionX = other.start().x();
        final int intersectionY = this.start().y();
        if (!containsClosedOnAxis(intersectionX) || !other.containsClosedOnAxis(intersectionY)) {
            // Intersection does not actually exist
            return false;
        } else {
            return containsOpenOnAxis(intersectionX) || other.containsOpenOnAxis(intersectionY);
        }
    }

    public List<WireSegment> splitAt(Vec2i point) {
        Preconditions.checkArgument(containsOpen(point));
        final int lengthFirst = axis.get(point) - axis.get(start);
        final int lengthSecond = length - lengthFirst;
        return ImmutableList.of(
                new WireSegment(start, lengthFirst, axis),
                new WireSegment(point, lengthSecond, axis)
        );
    }

    public boolean isOnExtendedWire(Vec2i point) {
        return axis.other().get(point) == axis.other().get(start);
    }

    public Vec2i[] getEnds() {
        return new Vec2i[]{start(), end()};
    }

    public void renderWithoutBlobs(PoseStack stack, int color) {
        ScreenUtils.fill(
                stack,
                start().x() + WIRE_SPACE, start().y() + WIRE_SPACE,
                end().x() + 1 - WIRE_SPACE, end().y() + 1 - WIRE_SPACE,
                color
        );
    }

    public RectangleI getShape() {
        return new RectangleI(start(), end().add(1, 1));
    }

    public enum WireAxis {
        X, Y;

        public static final MyCodec<WireAxis> CODEC = MyCodecs.BOOL.xmap(b -> b ? X : Y, a -> a == X);

        public Vec2i addToCoord(Vec2i fixed, int inAxisCoord) {
            if (this == X) {
                return new Vec2i(fixed.x() + inAxisCoord, fixed.y());
            } else {
                return new Vec2i(fixed.x(), fixed.y() + inAxisCoord);
            }
        }

        public int get(Vec2i vec) {
            return this == X ? vec.x() : vec.y();
        }

        public <T> T get(T x, T y) {
            return this == X ? x : y;
        }

        public WireAxis other() {
            return this == X ? Y : X;
        }
    }
}
