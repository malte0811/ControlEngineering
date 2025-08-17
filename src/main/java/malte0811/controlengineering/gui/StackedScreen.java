package malte0811.controlengineering.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
    public final void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderWithPrevious(graphics, mouseX, mouseY, partialTicks, true);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previousInStack);
        if (this instanceof MenuAccess<?> && previousInStack == null) {
            minecraft.player.closeContainer();
        }
    }

    @Override
    public final void renderBackground(@Nonnull GuiGraphics graphics) {
        super.renderBackground(graphics);
    }

    private void renderWithPrevious(
            @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, boolean isTop
    ) {
        if (previousInStack != null) {
            // Pretend the mouse is off-screen to stop button highlighting
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, -1);
            graphics.pose().scale(1, 1, 0.01f);
            previousInStack.renderWithPrevious(graphics, -1, -1, partialTicks, false);
            graphics.pose().popPose();
        }
        if (isTop) {
            renderBackground(graphics);
        }
        renderCustomBackground(graphics, mouseX, mouseY, partialTicks);
        renderForeground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected abstract void renderForeground(
            @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks
    );

    protected void renderCustomBackground(
            @Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks
    ) { }

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
