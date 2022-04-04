package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;

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
                new CellCost(2, 2)
        );
    }

    @Override
    protected CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        return CircuitSignals.singleton(
                DEFAULT_OUT_NAME, inputSignals.value(POSITIVE) >= inputSignals.value(NEGATIVE)
        );
    }
}
