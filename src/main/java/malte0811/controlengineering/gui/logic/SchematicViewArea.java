package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

import java.util.Objects;

import static malte0811.controlengineering.gui.logic.LogicDesignScreen.TOTAL_BORDER;

public class SchematicViewArea {
    public static final int BASE_SCALE = 3;

    private final Minecraft minecraft;
    private float minScale = 0.5F;
    private float currentScale = BASE_SCALE;
    // In schematic coordinates
    private double centerX = 0;
    private double centerY = 0;
    private float width;
    private float height;

    public SchematicViewArea(Minecraft minecraft) { this.minecraft = minecraft; }

    public void onSizeChanged(float width, float height) {
        this.width = width;
        this.height = height;
        minScale = Math.max(
                getScaleForShownSize(width, Schematic.BOUNDARY.getWidth()),
                getScaleForShownSize(height, Schematic.BOUNDARY.getHeight())
        );
        clampView();
    }

    public void clampView() {
        final double halfScreenWidth = getShownSizeForScale(width, currentScale) / 2;
        final double halfScreenHeight = getShownSizeForScale(height, currentScale) / 2;
        centerX = Mth.clamp(
                centerX, Schematic.GLOBAL_MIN + halfScreenWidth, Schematic.GLOBAL_MAX - halfScreenWidth
        );
        centerY = Mth.clamp(
                centerY, Schematic.GLOBAL_MIN + halfScreenHeight, Schematic.GLOBAL_MAX - halfScreenHeight
        );
    }

    public void setUpForDrawing(PoseStack matrixStack) {
        matrixStack.translate(width / 2., height / 2., 0);
        matrixStack.scale(currentScale, currentScale, 1);
        matrixStack.translate(-centerX, -centerY, 0);

        final double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor(
                (int) (TOTAL_BORDER * scale), (int) (TOTAL_BORDER * scale),
                (int) ((width - 2 * TOTAL_BORDER) * scale), (int) ((height - 2 * TOTAL_BORDER) * scale)
        );
    }

    public void move(double dragX, double dragY) {
        centerX -= dragX / currentScale;
        centerY -= dragY / currentScale;
        clampView();
    }

    public void onScroll(double delta) {
        final float zoomScale = 1.1f;
        if (delta > 0) {
            setScale(currentScale * zoomScale);
        } else {
            setScale(currentScale / zoomScale);
        }
        clampView();
    }

    public void autoRange(Schematic schematic) {
        var level = Objects.requireNonNull(Minecraft.getInstance().level);
        RectangleI totalArea = null;
        for (var symbol : schematic.getSymbols()) {
            totalArea = symbol.getShape(level).union(totalArea);
        }
        for (var net : schematic.getNets()) {
            for (var wire : net.getAllSegments()) {
                totalArea = wire.getShape().union(totalArea);
            }
        }
        if (totalArea == null) {
            totalArea = new RectangleI(-1, -1, 1, 1);
        }
        var center = totalArea.center();
        centerX = center.x();
        centerY = center.y();
        // Subtract 3x border to get a bit of space between circuit and actual border
        var scaleX = (width - 3 * TOTAL_BORDER) / (float) totalArea.getWidth();
        var scaleY = (height - 3 * TOTAL_BORDER) / (float) totalArea.getHeight();
        setScale(Math.min(Math.min(scaleX, scaleY), BASE_SCALE));
        clampView();
    }

    public Vec2d getMousePositionInSchematic(Vec2d screenPos) {
        return getMousePositionInSchematic(screenPos.x(), screenPos.y());
    }

    public Vec2d getMousePositionInSchematic(double mouseX, double mouseY) {
        return new Vec2d(
                (mouseX - width / 2.) / currentScale + centerX,
                (mouseY - height / 2.) / currentScale + centerY
        );
    }
    public float getCurrentScale() {
        return currentScale;
    }

    private static float getShownSizeForScale(float dimensionSize, float scale) {
        return (dimensionSize - 2 * TOTAL_BORDER - 5) / scale;
    }

    private static float getScaleForShownSize(float size, float shownSize) {
        return (size - 2 * TOTAL_BORDER - 5) / shownSize;
    }

    private void setScale(float newScale) {
        currentScale = Mth.clamp(newScale, minScale, 10);
    }
}
