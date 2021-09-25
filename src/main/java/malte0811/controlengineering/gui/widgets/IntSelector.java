package malte0811.controlengineering.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.gui.widget.ColorPicker16;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public class IntSelector extends StackedScreen {
    private final IntConsumer select;
    private final String translationKey;
    private BasicSlider lineSelect;

    public IntSelector(IntConsumer select, String translationKey) {
        super(new TextComponent("Signal selector"));
        this.select = select;
        this.translationKey = translationKey;
    }

    @Override
    protected void init() {
        super.init();
        lineSelect = new BasicSlider(
                width / 2 - 64,
                height / 2 + ColorPicker16.SIZE / 2 - 10,
                128,
                20,
                BusLine.MIN_VALID_VALUE,
                BusLine.MAX_VALID_VALUE,
                translationKey
        );
        addRenderableWidget(lineSelect);
        addRenderableWidget(new Button(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 + 20, 128, 20,
                new TranslatableComponent(DataProviderScreen.DONE_KEY), $ -> onClose()
        ));
    }

    @Override
    public void removed() {
        super.removed();
        select.accept(lineSelect.getValue());
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }
}
