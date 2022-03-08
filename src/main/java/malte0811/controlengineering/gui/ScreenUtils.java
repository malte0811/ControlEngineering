package malte0811.controlengineering.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ScreenUtils {
    public static void bindForShader(TextureAtlasSprite tas) {
        tas.atlas().bind();
        RenderSystem.setShaderTexture(0, tas.atlas().getId());
    }

    public static boolean isInRect(int xMin, int yMin, int width, int height, int x, int y) {
        return xMin <= x && x < xMin + width && yMin <= y && y < yMin + height;
    }
}
