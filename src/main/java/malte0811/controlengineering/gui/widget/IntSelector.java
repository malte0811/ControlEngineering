package malte0811.controlengineering.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public class IntSelector extends StackedScreen {
    private final IntConsumer select;
    private final String translationKey;
    private BasicSlider selector;
    private final int min;
    private final int max;
    private final int initial;

    public IntSelector(IntConsumer select, String translationKey, int min, int max, int initial) {
        super(new TextComponent("Signal selector"));
        this.select = select;
        this.translationKey = translationKey;
        this.min = min;
        this.max = max;
        this.initial = initial;
    }

    @Override
    protected void init() {
        super.init();
        selector = new BasicSlider(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 - 10, 128, 20, min, max, translationKey,
                selector != null ? selector.getValue() : initial
        );
        addRenderableWidget(selector);
        addRenderableWidget(new Button(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 + 20, 128, 20,
                new TranslatableComponent(DataProviderScreen.DONE_KEY), $ -> onClose()
        ));
    }

    @Override
    public void removed() {
        super.removed();
        select.accept(selector.getValue());
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }
}
