package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class NestedWidget extends AbstractWidget implements ContainerEventHandler {
    private final List<AbstractWidget> subWidgets = new ArrayList<>();
    private boolean isDragging;
    @Nullable
    private GuiEventListener listener;

    public NestedWidget(int x, int y, int width, int height) {
        super(x, y, width, height, TextComponent.EMPTY);
    }

    protected <T extends AbstractWidget> T addWidget(T newPart) {
        subWidgets.add(newPart);
        return newPart;
    }

    @Override
    public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (AbstractWidget w : subWidgets) {
            w.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Nonnull
    @Override
    public List<? extends GuiEventListener> children() {
        return subWidgets;
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        isDragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return listener;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return ContainerEventHandler.super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return ContainerEventHandler.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return ContainerEventHandler.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return ContainerEventHandler.super.charTyped(codePoint, modifiers);
    }

    @Override
    public void updateNarration(@Nonnull NarrationElementOutput pNarrationElementOutput) {}
}
