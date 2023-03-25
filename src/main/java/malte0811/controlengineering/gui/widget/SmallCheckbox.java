package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class SmallCheckbox extends Checkbox {
    private final BooleanConsumer onChange;

    public SmallCheckbox(
            int x, int y, int width, int height,
            Component text,
            boolean selected,
            BooleanConsumer onChange, Tooltip tooltip
    ) {
        super(x, y, width, height, text, selected, false);
        this.onChange = onChange;
        setTooltip(tooltip);
    }

    @Override
    public void onPress() {
        super.onPress();
        onChange.accept(selected());
    }

    @Override
    public void renderWidget(@Nonnull PoseStack transform, int mouseX, int mouseY, float partial) {
        transform.pushPose();
        transform.translate(getX(), getY() + width / 4., 0);
        transform.scale(0.5f, 0.5f, 1);
        transform.translate(-getX(), -getY(), 0);
        super.renderWidget(transform, mouseX, mouseY, partial);
        transform.popPose();
        var font = Minecraft.getInstance().font;
        drawString(
                transform, font, this.getMessage(),
                this.getX() + 12, this.getY() + (this.height - 8) / 2,
                0xffe0e0e0
        );
    }
}
