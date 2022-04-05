package malte0811.controlengineering.logic.cells.impl;

import malte0811.controlengineering.logic.cells.*;

import java.util.Map;

public class Multiplexer extends StatelessCell {
    public static final String INPUT_0 = "in0";
    public static final String INPUT_1 = "in1";
    public static final String OUTPUT = "out";
    public static final String SELECT = "select";

    public Multiplexer(SignalType type, CellCost cost) {
        super(
                Map.of(
                        INPUT_0, new Pin(type, PinDirection.INPUT),
                        INPUT_1, new Pin(type, PinDirection.INPUT),
                        SELECT, new Pin(SignalType.DIGITAL, PinDirection.INPUT)
                ),
                Map.of(OUTPUT, new Pin(type, PinDirection.OUTPUT)),
                cost
        );
    }

    @Override
    protected CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        return CircuitSignals.singleton(
                OUTPUT, inputSignals.value(inputSignals.bool(SELECT) ? INPUT_1 : INPUT_0)
        );
    }
}
