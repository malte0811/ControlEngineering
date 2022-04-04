package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class SchmittTrigger extends LeafcellType<Boolean> {
    public static final String LOW_PIN = "low";
    public static final String HIGH_PIN = "high";

    public SchmittTrigger() {
        super(
                ImmutableMap.of(
                        LOW_PIN, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        HIGH_PIN, new Pin(SignalType.ANALOG, PinDirection.INPUT)
                ),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                false,
                MyCodecs.BOOL,
                new CellCost(3, 4)
        );
    }

    @Override
    public Boolean nextState(CircuitSignals inputSignals, Boolean currentState) {
        if (currentState) {
            if (inputSignals.value(DEFAULT_IN_NAME) <= inputSignals.value(LOW_PIN)) {
                return false;
            }
        } else if (inputSignals.value(DEFAULT_IN_NAME) >= inputSignals.value(HIGH_PIN)) {
            return true;
        }
        return currentState;
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Boolean oldState) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, nextState(inputSignals, oldState));
    }
}
