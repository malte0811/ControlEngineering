package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.gui.widget.ColorPicker16;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;
import javax.annotation.Nullable;

public class BusSignalSelector extends DataProviderWidget<BusSignalRef> {
    public static final String BUS_LINE_INDEX_KEY = ControlEngineering.MODID + ".gui.lineIndex";
    public static final String COLOR_KEY = ControlEngineering.MODID + ".gui.signalColor";
    public static final int HEIGHT = 20 + ColorPicker16.SIZE;
    public static final int WIDTH = Math.max(128, ColorPicker16.SIZE);

    private final BasicSlider lineSelect;
    private final ColorPicker16 colorSelect;

    public BusSignalSelector(@Nullable BusSignalRef initial, int x, int y) {
        super(x, y, WIDTH, HEIGHT);
        lineSelect = new BasicSlider(
                x + width / 2 - 64, y + height / 2 + ColorPicker16.SIZE / 2 - 10,
                WIDTH, 20,
                0, BusWireType.NUM_LINES - 1,
                BUS_LINE_INDEX_KEY,
                initial != null ? initial.line : 0
        );
        colorSelect = new ColorPicker16(
                x + width / 2 - ColorPicker16.SIZE / 2,
                y + height / 2 - ColorPicker16.SIZE / 2 - ColorPicker16.TITLE_SPACE - 20,
                new TranslatableComponent(COLOR_KEY),
                initial != null ? DyeColor.byId(initial.color) : null
        );
        addWidget(lineSelect);
        addWidget(colorSelect);
    }

    @Override
    @Nullable
    public BusSignalRef getData() {
        DyeColor color = colorSelect.getSelected();
        if (color != null) {
            return new BusSignalRef(lineSelect.getValue(), color.getId());
        } else {
            return null;
        }
    }
}
