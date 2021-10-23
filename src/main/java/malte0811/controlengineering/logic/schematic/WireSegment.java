package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.GuiUtil;
import malte0811.controlengineering.util.math.Vec2i;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public record WireSegment(Vec2i start, int length,
                          malte0811.controlengineering.logic.schematic.WireSegment.WireAxis axis) {
    public static final Codec<WireSegment> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Vec2i.CODEC.fieldOf("start").forGetter(WireSegment::start),
                    Codec.INT.fieldOf("length").forGetter(WireSegment::length),
                    WireAxis.CODEC.fieldOf("axis").forGetter(WireSegment::axis)
            ).apply(inst, WireSegment::new)
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
        GuiUtil.fill(
                stack,
                start().x() + WIRE_SPACE, start().y() + WIRE_SPACE,
                end().x() + 1 - WIRE_SPACE, end().y() + 1 - WIRE_SPACE,
                color
        );
    }

    public enum WireAxis {
        X, Y;

        public static final Codec<WireAxis> CODEC = Codec.BOOL.xmap(b -> b ? X : Y, a -> a == X);

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
