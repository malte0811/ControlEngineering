package malte0811.controlengineering.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class GuiUtil {
    public static void fill(MatrixStack transform, double minX, double minY, double maxX, double maxY, int color) {
        Matrix4f matrix = transform.getLast().getMatrix();
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix, (float) minX, (float) maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) minX, (float) minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static Vec2d getMousePosition() {
        MouseHelper helper = Minecraft.getInstance().mouseHelper;
        MainWindow window = Minecraft.getInstance().getMainWindow();
        return new Vec2d(
                helper.getMouseX() * window.getScaledWidth() / (double) window.getWidth(),
                helper.getMouseY() * window.getScaledHeight() / (double) window.getHeight()
        );
    }
}
