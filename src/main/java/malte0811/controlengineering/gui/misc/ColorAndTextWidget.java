package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.gui.widget.ColorSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;

public class ColorAndTextWidget extends DataProviderWidget<ColorAndText> {
    private static final int TEXT_HEIGHT = 20;
    public static final int WIDTH = ColorSelector.WIDTH;
    public static final int HEIGHT = ColorSelector.HEIGHT + TEXT_HEIGHT;

    private final ColorSelector color;
    private final EditBox text;

    public ColorAndTextWidget(@Nullable ColorAndText initialState, int x, int y) {
        super(x, y, WIDTH, HEIGHT);
        addWidget(color = new ColorSelector(initialState != null ? initialState.color() : 0, x, y));
        addWidget(text = new EditBox(
                Minecraft.getInstance().font, x, y + ColorSelector.HEIGHT, ColorSelector.WIDTH, TEXT_HEIGHT,
                TextComponent.EMPTY
        ));
        text.setValue(initialState != null ? initialState.text() : "");
        text.setFilter(s -> s.chars().allMatch(i -> i < 128));
    }

    @Override
    @Nullable
    public ColorAndText getData() {
        final Integer color = this.color.getData();
        final String text = this.text.getValue().trim();
        if (color != null && !text.isEmpty()) {
            return new ColorAndText(color, text);
        } else {
            return null;
        }
    }
}
