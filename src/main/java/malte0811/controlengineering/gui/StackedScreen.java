package malte0811.controlengineering.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//TODO look at what giga did in Forge for this
public abstract class StackedScreen extends Screen {
    @Nullable
    private final StackedScreen previousInStack;
    @Nonnull
    protected Minecraft minecraft;

    protected StackedScreen(Component titleIn) {
        super(titleIn);
        this.minecraft = Minecraft.getInstance();
        Screen currentScreen = minecraft.screen;
        if (currentScreen instanceof StackedScreen) {
            this.previousInStack = (StackedScreen) currentScreen;
        } else {
            this.previousInStack = null;
        }
    }

    @Override
    public final void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderWithPrevious(matrixStack, mouseX, mouseY, partialTicks, true);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousInStack);
        if (this instanceof MenuAccess<?> && previousInStack == null) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public final void renderBackground(@Nonnull PoseStack matrixStack) {
        super.renderBackground(matrixStack);
    }

    private void renderWithPrevious(
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, boolean isTop
    ) {
        if (previousInStack != null) {
            // Pretend the mouse is off-screen to stop button highlighting
            matrixStack.pushPose();
            matrixStack.translate(0, 0, -1);
            matrixStack.scale(1, 1, 0.01f);
            previousInStack.renderWithPrevious(matrixStack, -1, -1, partialTicks, false);
            matrixStack.popPose();
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
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks
    );

    protected void renderCustomBackground(
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {}

    @Nullable
    public StackedScreen getPreviousInStack() {
        return previousInStack;
    }

    @Nullable
    public static <T extends StackedScreen> T findInstanceOf(Class<T> type) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (!(currentScreen instanceof StackedScreen)) {
            return null;
        }
        while (currentScreen != null && !type.isAssignableFrom(currentScreen.getClass())) {
            currentScreen = ((StackedScreen) currentScreen).getPreviousInStack();
        }
        return type.cast(currentScreen);
    }
}
