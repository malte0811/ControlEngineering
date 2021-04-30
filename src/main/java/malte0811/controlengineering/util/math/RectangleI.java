package malte0811.controlengineering.util.math;

public class RectangleI {
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public RectangleI(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public RectangleI(Vec2i min, Vec2i max) {
        this(min.x, min.y, max.x, max.y);
    }

    public boolean contains(RectangleI other) {
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

    public boolean disjoint(RectangleI other) {
        return minX >= other.maxX || other.minX >= maxX || minY >= other.maxY || other.minY >= maxY;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }
}
