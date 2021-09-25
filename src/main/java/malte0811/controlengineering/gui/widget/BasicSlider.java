package malte0811.controlengineering.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class BasicSlider extends AbstractSliderButton {
    private final int min;
    private final int max;
    private final String translationKey;

    public BasicSlider(int x, int y, int width, int height, int min, int max, String key) {
        this(x, y, width, height, min, max, key, min);
    }

    public BasicSlider(
            int x, int y, int width, int height, int min, int max, String key, int defaultValue
    ) {
        super(x, y, width, height, new TranslatableComponent(key, min), min + defaultValue / (double) (max - min));
        this.min = min;
        this.max = max;
        this.translationKey = key;
        updateMessage();
    }

    @Override
    // Update message
    protected void updateMessage() {
        setMessage(new TranslatableComponent(translationKey, getValue()));
    }

    @Override
    // Apply value
    protected void applyValue() {
        value = exactSliderValue();
    }

    public int getValue() {
        return (int) Math.round(Mth.clampedLerp(min, max, value));
    }

    private double exactSliderValue() {
        final int numValuesMin1 = max - min;
        return Math.round(value * numValuesMin1) / (double) numValuesMin1;
    }
}
