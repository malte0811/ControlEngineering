package malte0811.controlengineering.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.gui.widget.ColorPicker16;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class BusSignalSelector extends StackedScreen {
    public static final String BUS_LINE_INDEX_KEY = ControlEngineering.MODID + ".gui.lineIndex";
    public static final String COLOR_KEY = ControlEngineering.MODID + ".gui.signalColor";
    public static final String DONE_KEY = ControlEngineering.MODID + ".gui.done";

    private final Consumer<BusSignalRef> select;
    private BasicSlider lineSelect;
    private ColorPicker16 colorSelect;

    public BusSignalSelector(Consumer<BusSignalRef> select) {
        super(new StringTextComponent("Signal selector"));
        this.select = select;
    }

    @Override
    protected void init() {
        super.init();
        lineSelect = new BasicSlider(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 - 10,
                128, 20,
                0, BusWireType.NUM_LINES - 1,
                BUS_LINE_INDEX_KEY
        );
        colorSelect = new ColorPicker16(
                width / 2 - ColorPicker16.SIZE / 2,
                height / 2 - ColorPicker16.SIZE / 2 - ColorPicker16.TITLE_SPACE - 20,
                new TranslationTextComponent(COLOR_KEY)
        );
        addButton(lineSelect);
        addButton(colorSelect);
        addButton(new Button(
                width / 2 - 64, height / 2 + ColorPicker16.SIZE / 2 + 20, 128, 20,
                new TranslationTextComponent(DONE_KEY), $ -> closeScreen()
        ));
    }

    @Override
    public void onClose() {
        super.onClose();
        final DyeColor color = colorSelect.getSelected();
        if (color != null) {
            select.accept(new BusSignalRef(lineSelect.getValue(), color.getId()));
        }
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }
}
