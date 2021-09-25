package malte0811.controlengineering.util;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

public class GuiUtil {
    public static void fill(PoseStack transform, double minX, double minY, double maxX, double maxY, int color) {
        Matrix4f matrix = transform.last().pose();
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float) minX, (float) maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix, (float) maxX, (float) maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix, (float) maxX, (float) minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(matrix, (float) minX, (float) minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static Vec2d getMousePosition() {
        MouseHandler helper = Minecraft.getInstance().mouseHandler;
        Window window = Minecraft.getInstance().getWindow();
        return new Vec2d(
                helper.xpos() * window.getGuiScaledWidth() / (double) window.getScreenWidth(),
                helper.ypos() * window.getGuiScaledHeight() / (double) window.getScreenHeight()
        );
    }
}
