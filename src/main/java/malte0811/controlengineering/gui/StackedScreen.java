package malte0811.controlengineering.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class StackedScreen extends Screen {
    @Nullable
    private final StackedScreen previousInStack;
    @Nonnull
    protected Minecraft minecraft;

    protected StackedScreen(ITextComponent titleIn) {
        super(titleIn);
        this.minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.currentScreen;
        if (currentScreen instanceof StackedScreen) {
            this.previousInStack = (StackedScreen) currentScreen;
        } else {
            this.previousInStack = null;
        }
    }

    @Override
    public final void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderWithPrevious(matrixStack, mouseX, mouseY, partialTicks, true);
    }

    @Override
    public void closeScreen() {
        minecraft.displayGuiScreen(previousInStack);
    }

    @Override
    public final void renderBackground(@Nonnull MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
    }

    private void renderWithPrevious(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, boolean isTop
    ) {
        if (previousInStack != null) {
            // Pretend the mouse is off-screen to stop button highlighting
            previousInStack.renderWithPrevious(matrixStack, -1, -1, partialTicks, false);
        }
        if (isTop) {
            renderBackground(matrixStack);
        }
        renderCustomBackground(matrixStack, mouseX, mouseY, partialTicks);
        renderForeground(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected abstract void renderForeground(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
    );

    protected void renderCustomBackground(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {}

    @Nullable
    public StackedScreen getPreviousInStack() {
        return previousInStack;
    }

    @Nullable
    public static <T extends StackedScreen> T findInstanceOf(Class<T> type) {
        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (!(currentScreen instanceof StackedScreen)) {
            return null;
        }
        while (currentScreen != null && !type.isAssignableFrom(currentScreen.getClass())) {
            currentScreen = ((StackedScreen) currentScreen).getPreviousInStack();
        }
        return type.cast(currentScreen);
    }
}
