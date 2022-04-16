package malte0811.controlengineering.gui.widget;

import malte0811.controlengineering.gui.misc.DataProviderWidget;
import malte0811.controlengineering.gui.misc.IDataProviderWidget;
import malte0811.controlengineering.util.math.Fraction;

import javax.annotation.Nullable;

public class FractionSelector extends DataProviderWidget<Fraction> {
    private static final int WIDTH = 128;
    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_SPACE = 10;

    private final BasicSlider numerator;
    private final BasicSlider denominator;

    public FractionSelector(@Nullable Fraction initial, int x, int y, int max, String numKey, String denomKey) {
        super(x, y, WIDTH, 2 * SLIDER_HEIGHT + SLIDER_SPACE);
        numerator = addWidget(new BasicSlider(
                x, y, WIDTH, SLIDER_HEIGHT, 1, max, numKey, initial != null ? initial.numerator() : 1
        ));
        denominator = addWidget(new BasicSlider(
                x, y + SLIDER_HEIGHT + SLIDER_SPACE, WIDTH, SLIDER_HEIGHT,
                1, max,
                denomKey, initial != null ? initial.denominator() : 1
        ));
    }

    public static IDataProviderWidget.Factory<Fraction, FractionSelector> with(
            int max,
            String numKey,
            String denomKey
    ) {
        return (initial, x, y) -> new FractionSelector(initial, x, y, max, numKey, denomKey);
    }

    @Nullable
    @Override
    public Fraction getData() {
        final var numerator = this.numerator.getData();
        final var denominator = this.denominator.getData();
        if (numerator != null && denominator != null) {
            return new Fraction(numerator, denominator);
        } else {
            return null;
        }
    }
}
