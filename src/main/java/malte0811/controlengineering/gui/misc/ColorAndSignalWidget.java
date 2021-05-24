package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.gui.widget.ColorSelector;

import javax.annotation.Nullable;

public class ColorAndSignalWidget extends DataProviderWidget<ColorAndSignal> {
    public static final int WIDTH = ColorSelector.WIDTH + BusSignalSelector.WIDTH;
    public static final int HEIGHT = Math.max(ColorSelector.HEIGHT, BusSignalSelector.HEIGHT);

    private final ColorSelector color;
    private final BusSignalSelector signal;

    public ColorAndSignalWidget(@Nullable ColorAndSignal initialState, int x, int y) {
        super(x, y, WIDTH, HEIGHT);
        addWidget(color = new ColorSelector(initialState != null ? initialState.getColor() : 0, x, y));
        addWidget(signal = new BusSignalSelector(
                initialState != null ? initialState.getSignal() : null, x + ColorSelector.WIDTH, y
        ));
    }

    @Override
    @Nullable
    public ColorAndSignal getData() {
        final Integer color = this.color.getData();
        final BusSignalRef signal = this.signal.getData();
        if (color != null && signal != null) {
            return new ColorAndSignal(color, signal);
        } else {
            return null;
        }
    }
}
