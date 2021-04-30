package malte0811.controlengineering.util.math;

public class RectangleD {
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;

    public RectangleD(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public RectangleD(Vec2d min, Vec2d max) {
        this(min.x, min.y, max.x, max.y);
    }

    public boolean contains(RectangleD other) {
        return containsClosed(other.minX, other.minY) && containsClosed(other.maxX, other.maxY);
    }

    public boolean containsClosed(Vec2i point) {
        return containsClosed(point.x, point.y);
    }

    public boolean containsClosed(Vec2d point) {
        return containsClosed(point.x, point.y);
    }

    public boolean containsClosed(double x, double y) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY;
    }

    public boolean disjoint(RectangleD other) {
        return minX >= other.maxX || other.minX >= maxX || minY >= other.maxY || other.minY >= maxY;
    }

    public double getWidth() {
        return maxX - minX;
    }

    public double getHeight() {
        return maxY - minY;
    }
}
