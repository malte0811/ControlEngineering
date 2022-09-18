package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ScreenUtils {
    public static void fill(PoseStack transform, double minX, double minY, double maxX, double maxY, int color) {
        startPositionColorDraw();
        fillWithYOffsetDuringColorDraw(transform, minX, minY, minY, maxX, maxY - minY, color);
        endPositionColorDraw();
    }

    public static void fillWithYOffsetDuringColorDraw(
            PoseStack transform, double minX, double leftMinY, double rightMinY, double maxX, double height, int color
    ) {
        Matrix4f matrix = transform.last().pose();
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        final var bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.vertex(matrix, (float) minX, (float) (leftMinY + height), 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferbuilder.vertex(matrix, (float) maxX, (float) (rightMinY + height), 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferbuilder.vertex(matrix, (float) maxX, (float) rightMinY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();
        bufferbuilder.vertex(matrix, (float) minX, (float) leftMinY, 0.0F)
                .color(red, green, blue, alpha)
                .endVertex();
    }

    public static void endPositionColorDraw() {
        Tesselator.getInstance().end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void startPositionColorDraw() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    public static Vec2d getMousePosition() {
        MouseHandler helper = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        return new Vec2d(
                helper.xpos() * window.getGuiScaledWidth() / (double) window.getScreenWidth(),
                helper.ypos() * window.getGuiScaledHeight() / (double) window.getScreenHeight()
        );
    }

    public static void bindForShader(TextureAtlasSprite tas) {
        tas.atlas().bind();
        RenderSystem.setShaderTexture(0, tas.atlas().getId());
    }

    public static boolean isInRect(int xMin, int yMin, int width, int height, int x, int y) {
        return xMin <= x && x < xMin + width && yMin <= y && y < yMin + height;
    }

    public static void drawBordersOutside(
            PoseStack transform, int minX, int minY, int maxX, int maxY, float borderWidth, int color
    ) {
        fill(transform, minX - borderWidth, minY - borderWidth, maxX + borderWidth, minY, color);
        fill(transform, minX - borderWidth, minY - borderWidth, minX, maxY + borderWidth, color);
        fill(transform, maxX, minY - borderWidth, maxX + borderWidth, maxY + borderWidth, color);
        fill(transform, minX - borderWidth, maxY, maxX + borderWidth, maxY + borderWidth, color);
    }

    public static void setupScissorMCScaled(double minX, double minY, double width, double height) {
        final double scale = Minecraft.getInstance().getWindow().getGuiScale();
        final var actualMinY = Minecraft.getInstance().getWindow().getGuiScaledHeight() - minY - height;
        RenderSystem.enableScissor(
                (int) (minX * scale), (int) (actualMinY * scale), (int) (width * scale), (int) (height * scale)
        );
    }
}
