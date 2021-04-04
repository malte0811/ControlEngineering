package malte0811.controlengineering.gui.widget;

import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

public class BasicSlider extends AbstractSlider {
    private final int min;
    private final int max;
    private final String translationKey;

    public BasicSlider(
            int x, int y, int width, int height, int min, int max, String key
    ) {
        super(x, y, width, height, new TranslationTextComponent(key, min), 0);
        this.min = min;
        this.max = max;
        this.translationKey = key;
    }

    @Override
    // Update message
    protected void func_230979_b_() {
        setMessage(new TranslationTextComponent(translationKey, getValue()));
    }

    @Override
    // Apply value
    protected void func_230972_a_() {
        sliderValue = exactSliderValue();
    }

    public int getValue() {
        return (int) Math.round(MathHelper.clampedLerp(min, max, sliderValue));
    }

    private double exactSliderValue() {
        final int numValuesMin1 = max - min;
        return Math.round(sliderValue * numValuesMin1) / (double) numValuesMin1;
    }
}
