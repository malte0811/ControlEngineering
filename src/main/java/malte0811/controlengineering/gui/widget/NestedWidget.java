package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class NestedWidget extends Widget implements INestedGuiEventHandler {
    private final List<Widget> subWidgets = new ArrayList<>();
    private boolean isDragging;
    @Nullable
    private IGuiEventListener listener;

    public NestedWidget(int x, int y, int width, int height) {
        super(x, y, width, height, StringTextComponent.EMPTY);
    }

    protected void addWidget(Widget newPart) {
        subWidgets.add(newPart);
    }

    @Override
    public void renderWidget(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for (Widget w : subWidgets) {
            w.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    @Nonnull
    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
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
    public IGuiEventListener getListener() {
        return listener;
    }

    @Override
    public void setListener(@Nullable IGuiEventListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return INestedGuiEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return INestedGuiEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return INestedGuiEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return INestedGuiEventHandler.super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return INestedGuiEventHandler.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return INestedGuiEventHandler.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return INestedGuiEventHandler.super.charTyped(codePoint, modifiers);
    }
}
