package malte0811.controlengineering.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;

public class SubTexture {
    private final ResourceLocation mainTexture;
    private final int minU;
    private final int minV;
    private final int maxU;
    private final int maxV;

    public SubTexture(ResourceLocation mainTexture, int minU, int minV, int maxU, int maxV) {
        this.mainTexture = mainTexture;
        this.minU = minU;
        this.minV = minV;
        this.maxU = maxU;
        this.maxV = maxV;
    }

    public int getMaxV() {
        return maxV;
    }

    public int getMaxU() {
        return maxU;
    }

    public int getMinV() {
        return minV;
    }

    public int getMinU() {
        return minU;
    }

    public int getHeight() {
        return maxV - minV;
    }

    public int getWidth() {
        return maxU - minU;
    }

    public ResourceLocation getMainTexture() {
        return mainTexture;
    }

    public ImageButton createButton(int x, int y, Button.IPressable onPress) {
        return new ImageButton(
                x, y,
                getWidth(), getHeight(), getMinU(), getMinV(),
                0, getMainTexture(),
                onPress
        );
    }

    public void blit(MatrixStack transform, int x, int y) {
        Minecraft.getInstance().getTextureManager().bindTexture(getMainTexture());
        Screen.blit(
                transform, x, y, getWidth(), getHeight(), getMinU(), getMinV(), getWidth(), getHeight(),
                256, 256
        );
    }
}
