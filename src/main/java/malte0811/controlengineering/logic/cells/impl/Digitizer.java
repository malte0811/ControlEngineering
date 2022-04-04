package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.logic.cells.*;

public class Digitizer extends StatelessCell {
    public Digitizer() {
        super(
                ImmutableMap.of(DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                new CellCost(1, 1)
        );
    }

    @Override
    protected CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, digitize(inputSignals.value(DEFAULT_IN_NAME)));
    }

    public static int digitize(int in) {
        return in > BusLine.MAX_VALID_VALUE / 2 ? BusLine.MAX_VALID_VALUE : BusLine.MIN_VALID_VALUE;
    }
}
