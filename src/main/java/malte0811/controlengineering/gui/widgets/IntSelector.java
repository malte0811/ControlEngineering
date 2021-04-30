package malte0811.controlengineering.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.gui.widget.ColorPicker16;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.function.IntConsumer;

public class IntSelector extends StackedScreen {
    private final IntConsumer select;
    private final String translationKey;
    private BasicSlider lineSelect;

    public IntSelector(IntConsumer select, String translationKey) {
        super(new StringTextComponent("Signal selector"));
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
        addButton(lineSelect);
        addButton(new Button(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 + 20, 128, 20,
                new TranslationTextComponent(DataProviderScreen.DONE_KEY), $ -> closeScreen()
        ));
    }

    @Override
    public void onClose() {
        super.onClose();
        select.accept(lineSelect.getValue());
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }
}
