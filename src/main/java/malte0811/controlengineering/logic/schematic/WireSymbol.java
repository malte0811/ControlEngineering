package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;

public class WireSymbol implements SchematicSymbol {
    private static final int WIDTH = 1;
    private static final int WIRE_COLOR = 0xfff0aa2a;

    private final Axis axis;
    private final int length;
    private boolean blobAtStart = false;
    private boolean blobAtEnd = false;

    public WireSymbol(Axis axis, int length) {
        this.axis = axis;
        this.length = length;
    }

    @Override
    public void render(MatrixStack transform, int x, int y) {
        AbstractGui.fill(
                transform, x, y, x + getXSize(), y + getYSize(), WIRE_COLOR
        );
        if (blobAtEnd) {
            int xEnd = axis.getXSize(length, 0) + x;
            int yEnd = axis.getYSize(length, 0) + y;
            AbstractGui.fill(transform, xEnd - WIDTH, yEnd - WIDTH, xEnd + 2 * WIDTH, yEnd + 2 * WIDTH, WIRE_COLOR);
        }
        if (blobAtStart) {
            AbstractGui.fill(transform, x - WIDTH, y - WIDTH, x + 2 * WIDTH, y + 2 * WIDTH, WIRE_COLOR);
        }
    }

    @Override
    public int getXSize() {
        return axis.getXSize(length, WIDTH);
    }

    @Override
    public int getYSize() {
        return axis.getYSize(length, WIDTH);
    }

    @Override
    public boolean allowIntersecting() {
        return true;
    }

    public enum Axis {
        X, Y;

        public int getXSize(int inAxis, int otherAxis) {
            return X == this ? inAxis : otherAxis;
        }

        public int getYSize(int inAxis, int otherAxis) {
            return Y == this ? inAxis : otherAxis;
        }
    }
}
