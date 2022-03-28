package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.gui.widget.ColorSelector;

import javax.annotation.Nullable;

public class ColorAndTextWidget extends DataProviderWidget<ColorAndText> {
    private static final int TEXT_HEIGHT = 20;
    public static final int WIDTH = ColorSelector.WIDTH;
    public static final int HEIGHT = ColorSelector.HEIGHT + TEXT_HEIGHT;

    private final ColorSelector color;
    private final TextProviderWidget text;

    public ColorAndTextWidget(@Nullable ColorAndText initialState, int x, int y) {
        super(x, y, WIDTH, HEIGHT);
        addWidget(color = new ColorSelector(initialState != null ? initialState.color() : 0, x, y));
        addWidget(text = TextProviderWidget.tapeSerializable(
                initialState != null ? initialState.text() : "", x, y + ColorSelector.HEIGHT
        ));
    }

    @Override
    @Nullable
    public ColorAndText getData() {
        final Integer color = this.color.getData();
        final String text = this.text.getData();
        if (color != null && text != null) {
            return new ColorAndText(color, text);
        } else {
            return null;
        }
    }
}
