package malte0811.controlengineering.logic.schematic;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.AbstractGui;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class WireSegment {
    public static final Codec<WireSegment> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Vec2i.CODEC.fieldOf("start").forGetter(WireSegment::getStart),
                    Codec.INT.fieldOf("length").forGetter(WireSegment::getLength),
                    WireAxis.CODEC.fieldOf("axis").forGetter(WireSegment::getAxis)
            ).apply(inst, WireSegment::new)
    );

    private final Vec2i start;
    private final int length;
    private final WireAxis axis;

    public WireSegment(Vec2i start, int length, WireAxis axis) {
        this.start = start;
        this.length = length;
        this.axis = axis;
    }

    public Vec2i getStart() {
        return start;
    }

    public Vec2i getEnd() {
        return axis.addToCoord(start, length);
    }

    public int getLength() {
        return length;
    }

    public WireAxis getAxis() {
        return axis;
    }

    public boolean containsClosed(Vec2i point) {
        if (!isOnExtendedWire(point)) {
            return false;
        }
        final int min = axis.get(start);
        final int pos = axis.get(point);
        return pos >= min && pos <= min + length;
    }

    public boolean containsOpen(Vec2i point) {
        if (!isOnExtendedWire(point)) {
            return false;
        }
        final int min = axis.get(start);
        final int pos = axis.get(point);
        return pos > min && pos < min + length;
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

    private boolean isOnExtendedWire(Vec2i point) {
        return axis.other().get(point) == axis.other().get(start);
    }

    public Vec2i[] getEnds() {
        return new Vec2i[]{getStart(), getEnd()};
    }

    public void renderWithoutBlobs(MatrixStack stack, int color) {
        AbstractGui.fill(stack, getStart().x, getStart().y, getEnd().x + 1, getEnd().y + 1, color);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WireSegment that = (WireSegment) o;
        return length == that.length && start.equals(that.start) && axis == that.axis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, length, axis);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WireSegment.class.getSimpleName() + "[", "]")
                .add("start=" + start)
                .add("length=" + length)
                .add("axis=" + axis)
                .toString();
    }

    public enum WireAxis {
        X, Y;

        public static final Codec<WireAxis> CODEC = Codec.BOOL.xmap(b -> b ? X : Y, a -> a == X);

        public Vec2i addToCoord(Vec2i fixed, int inAxisCoord) {
            if (this == X) {
                return new Vec2i(fixed.x + inAxisCoord, fixed.y);
            } else {
                return new Vec2i(fixed.x, fixed.y + inAxisCoord);
            }
        }

        public int get(Vec2i vec) {
            return this == X ? vec.x : vec.y;
        }

        public <T> T get(T x, T y) {
            return this == X ? x : y;
        }

        public WireAxis other() {
            return this == X ? Y : X;
        }
    }
}
