package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class Comparator extends StatelessCell {
    public static final String POSITIVE = "positive";
    public static final String NEGATIVE = "negative";

    public Comparator() {
        super(
                ImmutableMap.of(
                        POSITIVE, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        NEGATIVE, new Pin(SignalType.ANALOG, PinDirection.INPUT)
                ),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                //TODO
                2
        );
    }

    @Override
    protected Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        return Object2DoubleMaps.singleton(
                DEFAULT_OUT_NAME, debool(inputSignals.getDouble(POSITIVE) >= inputSignals.getDouble(NEGATIVE))
        );
    }
}
