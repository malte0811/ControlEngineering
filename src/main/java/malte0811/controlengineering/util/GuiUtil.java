package malte0811.controlengineering.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

public class GuiUtil {
    public static void fill(MatrixStack transform, float minX, float minY, float maxX, float maxY, int color) {
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
        bufferbuilder.pos(matrix, minX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, maxX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, maxX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, minX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
