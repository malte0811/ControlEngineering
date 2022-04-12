package malte0811.controlengineering.gui.widget;

import malte0811.controlengineering.gui.misc.IDataProviderWidget;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class BasicSlider extends AbstractSliderButton implements IDataProviderWidget<Integer> {
    private final int min;
    private final int max;
    private final String translationKey;

    public BasicSlider(int x, int y, int width, int height, int min, int max, String key) {
        this(x, y, width, height, min, max, key, min);
    }

    public BasicSlider(
            int x, int y, int width, int height, int min, int max, String key, int defaultValue
    ) {
        super(x, y, width, height, new TranslatableComponent(key, min), (defaultValue - min) / (double) (max - min));
        this.min = min;
        this.max = max;
        this.translationKey = key;
        updateMessage();
    }

    public static IDataProviderWidget.Factory<Integer, BasicSlider> withRange(int min, int max, String key) {
        return (initial, x, y) -> new BasicSlider(x, y, 128, 20, min, max, key, initial == null ? min : initial);
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

    @Nullable
    @Override
    public Integer getData() {
        return getValue();
    }
}
