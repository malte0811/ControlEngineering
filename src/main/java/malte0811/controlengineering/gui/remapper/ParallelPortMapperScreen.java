package malte0811.controlengineering.gui.remapper;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.util.math.Vec2i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelPortMapperScreen extends AbstractRemapperScreen {
    private static final int LEFT_Y_MIN = 40;
    private static final int LEFT_X_MIN = 10;
    private static final int RIGHT_X_MIN = 110;
    private static final int RIGHT_Y_MIN = 13;
    private static final int SPACE_BETWEEN_LINES = 7;
    private static final int SPACE_TO_LABEL = 6;
    private static final SubTexture LEFT_TEXTURE = new SubTexture(TEXTURE, 244, 128, 256, 202);
    private static final SubTexture LINE_COLORS = new SubTexture(TEXTURE, 198, 229, 224, 256);
    private static final SubTexture BIG_WRAPPED_WIRE = RSRemapperScreen.WRAPPED_WIRE;
    private static final SubTexture SMALL_WRAPPED_WIRE = new SubTexture(TEXTURE, 224, 254, 227, 256);
    private static final SubTexture[] LINE_LABELS = {
            new SubTexture(TEXTURE, 228, 228, 242, 242),
            new SubTexture(TEXTURE, 242, 228, 256, 242),
            new SubTexture(TEXTURE, 228, 242, 242, 256),
            new SubTexture(TEXTURE, 242, 242, 256, 256),
    };
    private static final SubTexture OPEN_DOOR = new SubTexture(TEXTURE, 0, 0, 47, 185);

    public ParallelPortMapperScreen(AbstractRemapperMenu menu) {
        super(menu, makeSourcePoints(), makeTargetPoints());
    }

    @Override
    public void renderBackground(@Nonnull PoseStack transform) {
        super.renderBackground(transform);
        transform.pushPose();
        transform.translate(leftPos, topPos, 0);
        OPEN_DOOR.blit(transform, -38, -31);
        LEFT_TEXTURE.blit(transform, LEFT_X_MIN, LEFT_Y_MIN);
        for (int line = 0; line < BusWireType.NUM_LINES; ++line) {
            final var lineY = getLineY(line);
            LINE_COLORS.blit(transform, RIGHT_X_MIN, lineY);
            final int labelX = RIGHT_X_MIN + LINE_COLORS.getWidth() + SPACE_TO_LABEL;
            final int labelY = lineY + (LINE_COLORS.getHeight() - LINE_LABELS[line].getHeight()) / 2;
            LINE_LABELS[line].blit(transform, labelX, labelY);
        }
        transform.popPose();
    }

    private static List<ConnectionPoint> makeSourcePoints() {
        List<ConnectionPoint> points = new ArrayList<>(9);
        for (int y : new int[]{2, 10, 18, 28, 36, 44, 52, 60, 68}) {
            points.add(new ConnectionPoint(
                    true, points.size(), new Vec2i(LEFT_X_MIN + 8, LEFT_Y_MIN + y), BIG_WRAPPED_WIRE
            ));
        }
        return points;
    }

    private static List<ConnectionPoint> makeTargetPoints() {
        ConnectionPoint[] points = new ConnectionPoint[64];
        for (int line = 0; line < BusWireType.NUM_LINES; ++line) {
            for (int color = 0; color < BusLine.LINE_SIZE; color++) {
                final int x = RIGHT_X_MIN + 1 + 7 * (color % 4);
                final int y = getLineY(line) + 1 + 7 * (color / 4);
                final int index = new BusSignalRef(line, color).index();
                points[index] = new ConnectionPoint(false, index, new Vec2i(x, y), SMALL_WRAPPED_WIRE);
            }
        }
        return Arrays.asList(points);
    }

    private static int getLineY(int line) {
        return RIGHT_Y_MIN + line * (LINE_COLORS.getHeight() + SPACE_BETWEEN_LINES);
    }
}
