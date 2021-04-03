package malte0811.controlengineering.gui.tape;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public class ViewTapeScreen extends Screen {
    public static final ResourceLocation BASE_SCREEN = new ResourceLocation(
            ControlEngineering.MODID,
            "textures/gui/read_tape.png"
    );
    private static final int WIDTH = 256;
    private static final int HEIGHT = 128;
    private static final int FIRST_CHAR_X = 48;
    private static final int FIRST_HOLE_Y = 65;
    private static final int NUM_VISIBLE_CHARS = 27;
    private final byte[] fullData;
    private int offset = 0;
    private final TapeRender tapeRender;

    public ViewTapeScreen(String titleIn, byte[] data) {
        super(new StringTextComponent(titleIn));
        this.fullData = data;
        this.tapeRender = new TapeRender(FIRST_CHAR_X, FIRST_HOLE_Y, () -> font, getShownBytes());
    }

    @Override
    protected void init() {
        super.init();
        final int buttonWidth = 20;
        final int leftButtonX = FIRST_CHAR_X - 30 + (this.width - WIDTH) / 2;
        final int buttonY = FIRST_HOLE_Y + 3 + (this.height - HEIGHT) / 2;
        addButton(new Button(
                leftButtonX + 1, buttonY, buttonWidth, 20,
                new StringTextComponent("<"),
                btn -> incOffset()
        ));
        addButton(new Button(
                width - leftButtonX - buttonWidth + 1, buttonY, buttonWidth, 20,
                new StringTextComponent(">"),
                btn -> decOffset()
        ));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BASE_SCREEN);
        {
            int startX = (this.width - WIDTH) / 2;
            int startY = (this.height - HEIGHT) / 2;
            matrixStack.push();
            matrixStack.translate(startX, startY, 0);
        }
        blit(matrixStack, 0, 0, 0, 0, WIDTH, HEIGHT);
        tapeRender.render(matrixStack);
        matrixStack.pop();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0) {
            incOffset();
        } else {
            decOffset();
        }
        return true;
    }

    private byte[] getShownBytes() {
        byte[] result = new byte[NUM_VISIBLE_CHARS];
        for (int i = 0; i < NUM_VISIBLE_CHARS; ++i) {
            int actualIndex = i + offset;
            if (actualIndex < fullData.length && actualIndex >= 0) {
                result[i] = fullData[actualIndex];
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void incOffset() {
        if (offset < Math.max(fullData.length - NUM_VISIBLE_CHARS / 2, 0)) {
            ++offset;
        }
        tapeRender.setData(getShownBytes());
    }

    private void decOffset() {
        if (offset > -NUM_VISIBLE_CHARS / 2) {
            --offset;
        }
        tapeRender.setData(getShownBytes());
    }
}
