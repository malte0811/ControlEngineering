package malte0811.controlengineering.gui.remapper;

import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class RSRemapperScreen extends AbstractRemapperScreen {
    private static final int WRAP_X_COLOR = 19;
    private static final int WRAP_X_GRAY = 142;
    private static final int FIRST_WRAP_Y = 15;
    private static final int COLOR_HEIGHT = 8;
    public static final SubTexture WRAPPED_WIRE = new SubTexture(TEXTURE, 232, 128, 235, 132);
    private static final SubTexture POINTS_LEFT = new SubTexture(TEXTURE, 232, 0, 244, 128);
    private static final SubTexture POINTS_RIGHT = new SubTexture(TEXTURE, 244, 0, 256, 128);

    public RSRemapperScreen(AbstractRemapperMenu menu) {
        super(menu, makePoints(true), makePoints(false));
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos, topPos, 0);
        POINTS_LEFT.blit(graphics.pose(), WRAP_X_COLOR - 8, FIRST_WRAP_Y - 2);
        POINTS_RIGHT.blit(graphics.pose(), WRAP_X_GRAY - 1, FIRST_WRAP_Y - 2);
        graphics.pose().popPose();
    }

    private static List<ConnectionPoint> makePoints(boolean color) {
        List<ConnectionPoint> pointsOnSide = new ArrayList<>();
        for (int i = 0; i < BusLine.LINE_SIZE; ++i) {
            var yMin = FIRST_WRAP_Y + i * COLOR_HEIGHT;
            var xMin = color ? WRAP_X_COLOR : WRAP_X_GRAY;
            pointsOnSide.add(new ConnectionPoint(color, i, new Vec2i(xMin, yMin), WRAPPED_WIRE));
        }
        return pointsOnSide;
    }
}
