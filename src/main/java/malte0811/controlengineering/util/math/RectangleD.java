package malte0811.controlengineering.util.math;

public record RectangleD(double minX, double minY, double maxX, double maxY) {

    public RectangleD(Vec2d min, Vec2d max) {
        this(min.x(), min.y(), max.x(), max.y());
    }

    public boolean contains(RectangleD other) {
        return containsClosed(other.minX, other.minY) && containsClosed(other.maxX, other.maxY);
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
