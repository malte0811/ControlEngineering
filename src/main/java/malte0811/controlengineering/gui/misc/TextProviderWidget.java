package malte0811.controlengineering.gui.misc;

import malte0811.controlengineering.gui.widget.ColorSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class TextProviderWidget extends DataProviderWidget<String> {
    public static final int HEIGHT = 20;
    private final EditBox text;

    public static TextProviderWidget tapeSerializable(@Nullable String initialState, int x, int y) {
        return new TextProviderWidget(initialState, x, y, s -> s.chars().allMatch(i -> i < 128));
    }

    public static TextProviderWidget arbitrary(@Nullable String initialState, int x, int y) {
        return new TextProviderWidget(initialState, x, y, $ -> true);
    }

    private TextProviderWidget(@Nullable String initialState, int x, int y, Predicate<String> isValid) {
        super(x, y, ColorSelector.WIDTH, HEIGHT);
        addWidget(text = new EditBox(
                Minecraft.getInstance().font,
                x, y, ColorSelector.WIDTH, HEIGHT,
                TextComponent.EMPTY
        ));
        text.setValue(initialState != null ? initialState : "");
        text.setFilter(isValid);
    }

    @Nullable
    @Override
    public String getData() {
        final String text = this.text.getValue().trim();
        return text.isEmpty() ? null : text;
    }
}
