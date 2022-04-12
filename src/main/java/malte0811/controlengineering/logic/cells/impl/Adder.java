package malte0811.controlengineering.logic.cells.impl;

import malte0811.controlengineering.logic.cells.*;

import java.util.Map;

public class Adder extends StatelessCell {
    public static final String IN_A = "in_a";
    public static final String IN_B = "in_b";
    public static final String OUTPUT = DEFAULT_OUT_NAME;

    public Adder() {
        super(
                Map.of(
                        IN_A, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        IN_B, new Pin(SignalType.ANALOG, PinDirection.INPUT)
                ),
                Map.of(OUTPUT, new Pin(SignalType.ANALOG, PinDirection.OUTPUT)),
                new CellCost(2, 4)
        );
    }

    @Override
    protected CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        return CircuitSignals.singleton(OUTPUT, -(inputSignals.value(IN_A) + inputSignals.value(IN_B)));
    }
}
