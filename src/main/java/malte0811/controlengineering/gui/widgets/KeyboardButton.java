package malte0811.controlengineering.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.CharConsumer;
import malte0811.controlengineering.gui.Keyboard;
import malte0811.controlengineering.gui.SubTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

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
                StringTextComponent.EMPTY,
                btn -> pressedAction.accept(((KeyboardButton) btn).getChar())
        );
        this.texture = texture;
        this.lowerCase = lowerCase;
        this.caps = caps;
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(this.texture.getMainTexture());
        RenderSystem.enableDepthTest();
        texture.blit(matrixStack, x, y);
        drawCenteredString(
                matrixStack,
                minecraft.fontRenderer,
                this.getMessage(),
                this.x + this.width / 2, this.y + (this.height - 8) / 2,
                -1
        );
    }

    @Nonnull
    @Override
    public ITextComponent getMessage() {
        return new StringTextComponent("" + getChar());
    }

    public char getChar() {
        return Keyboard.convert(lowerCase, caps.getAsBoolean() ^ Screen.hasShiftDown());
    }
}
