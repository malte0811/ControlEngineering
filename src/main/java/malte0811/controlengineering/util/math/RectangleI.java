package malte0811.controlengineering.util.math;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec4;

import javax.annotation.Nullable;

public record RectangleI(int minX, int minY, int maxX, int maxY) {
    public static final MyCodec<RectangleI> CODEC = new RecordCodec4<>(
            MyCodecs.INTEGER.fieldOf("minX", RectangleI::minX),
            MyCodecs.INTEGER.fieldOf("minY", RectangleI::minY),
            MyCodecs.INTEGER.fieldOf("maxX", RectangleI::maxX),
            MyCodecs.INTEGER.fieldOf("maxY", RectangleI::maxY),
            RectangleI::new
    );

    public RectangleI(Vec2i min, Vec2i max) {
        this(min.x(), min.y(), max.x(), max.y());
    }

    public RectangleI {
        final var realMinX = Math.min(minX, maxX);
        final var realMaxX = Math.max(minX, maxX);
        final var realMinY = Math.min(minY, maxY);
        final var realMaxY = Math.max(minY, maxY);
        minX = realMinX;
        maxX = realMaxX;
        minY = realMinY;
        maxY = realMaxY;
    }

    public boolean contains(RectangleI other) {
        return containsClosed(other.minX, other.minY) && containsClosed(other.maxX, other.maxY);
    }

    public boolean containsClosed(Vec2i point) {
        return containsClosed(point.x(), point.y());
    }

    public boolean containsClosed(Vec2d point) {
        return containsClosed(point.x(), point.y());
    }

    public boolean containsClosed(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean disjoint(RectangleI other) {
        return minX >= other.maxX || other.minX >= maxX || minY >= other.maxY || other.minY >= maxY;
    }

    public RectangleI union(@Nullable RectangleI other) {
        if (other == null) {
            return this;
        }
        return new RectangleI(
                Math.min(minX(), other.minX()), Math.min(minY(), other.minY()),
                Math.max(maxX(), other.maxX()), Math.max(maxY(), other.maxY())
        );
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public Vec2d center() {
        return new Vec2d(minX() + getWidth() / 2., minY() + getHeight() / 2.);
    }

    public RectangleI offset(Vec2i by) {
        return new RectangleI(minX() + by.x(), minY() + by.y(), maxX() + by.x(), maxY + by.y());
    }
}
