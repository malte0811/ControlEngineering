package malte0811.controlengineering.gui.widget;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.misc.DataProviderWidget;
import malte0811.controlengineering.util.ColorUtils;

import javax.annotation.Nullable;

public class ColorSelector extends DataProviderWidget<Integer> {
    public static final String RED = ControlEngineering.MODID + ".gui.red";
    public static final String GREEN = ControlEngineering.MODID + ".gui.green";
    public static final String BLUE = ControlEngineering.MODID + ".gui.blue";
    public static final int HEIGHT = 60;
    public static final int WIDTH = 128;

    private final BasicSlider red;
    private final BasicSlider green;
    private final BasicSlider blue;

    public ColorSelector(int colorIn, int x, int y) {
        super(x, y, WIDTH, HEIGHT);
        final int sliderHeight = HEIGHT / 3;
        addWidget(red = new BasicSlider(x, y, width, sliderHeight, 0, 255, RED, ColorUtils.getRed(colorIn)));
        addWidget(green = new BasicSlider(
                x, y + sliderHeight, width, sliderHeight, 0, 255, GREEN, ColorUtils.getGreen(colorIn)
        ));
        addWidget(blue = new BasicSlider(
                x, y + 2 * sliderHeight, width, sliderHeight, 0, 255, BLUE, ColorUtils.getBlue(colorIn)
        ));
    }

    @Nullable
    @Override
    public Integer getData() {
        return red.getValue() << 16 | green.getValue() << 8 | blue.getValue();
    }
}
