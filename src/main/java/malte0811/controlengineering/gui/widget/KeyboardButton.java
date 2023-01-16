package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import malte0811.controlengineering.gui.SubTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class KeyboardButton extends Button {
    private final SubTexture texture;
    private final char lowerCase;
    private final BooleanSupplier caps;

    public KeyboardButton(
            int x,
            int y,
            CharConsumer pressedAction,
            SubTexture texture,
            char lowerCase,
            BooleanSupplier caps
    ) {
        super(
                x, y, texture.getWidth(), texture.getHeight(),
                Component.empty(),
                btn -> pressedAction.accept(((KeyboardButton) btn).getChar()),
                p_253695_ -> Component.empty()
        );
        this.texture = texture;
        this.lowerCase = lowerCase;
        this.caps = caps;
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, this.texture.getMainTexture());
        RenderSystem.enableDepthTest();
        texture.blit(matrixStack, getX(), getY());
        drawCenteredString(
                matrixStack,
                minecraft.font,
                this.getMessage(),
                this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2,
                -1
        );
    }

    @Nonnull
    @Override
    public Component getMessage() {
        return Component.literal("" + getChar());
    }

    public char getChar() {
        return Keyboard.convert(lowerCase, caps.getAsBoolean() ^ Screen.hasShiftDown());
    }
}
