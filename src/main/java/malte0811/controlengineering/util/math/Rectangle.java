package malte0811.controlengineering.util.math;

public class Rectangle {
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public Rectangle(int minX, int minY, int maxX, int maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Rectangle(Vec2i min, Vec2i max) {
        this(min.x, min.y, max.x, max.y);
    }

    public boolean contains(Rectangle other) {
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

    public boolean disjoint(Rectangle other) {
        return minX >= other.maxX || other.minX >= maxX || minY >= other.maxY || other.minY >= maxY;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }
}
