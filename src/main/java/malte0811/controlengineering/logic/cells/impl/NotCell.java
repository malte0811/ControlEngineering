package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;

public class NotCell extends StatelessCell {
    private static final String IN_NAME = "in";

    public NotCell() {
        super(
                ImmutableMap.of(IN_NAME, new Pin(SignalType.DIGITAL, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                new CellCost(1, 0.5)
        );
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, !inputSignals.bool(IN_NAME));
    }
}
