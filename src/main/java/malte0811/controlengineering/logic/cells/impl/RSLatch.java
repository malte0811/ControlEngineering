package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.Random;

public class RSLatch extends LeafcellType<Boolean> {
    private static final Random RAND = new Random();
    public static final String RESET = "reset";
    public static final String SET = "set";
    public static final String Q = "q";
    public static final String NOT_Q = "not_q";

    public RSLatch() {
        super(
                ImmutableMap.of(
                        RESET, new Pin(SignalType.DIGITAL, PinDirection.INPUT),
                        SET, new Pin(SignalType.DIGITAL, PinDirection.INPUT)
                ),
                ImmutableMap.of(
                        Q, new Pin(SignalType.DIGITAL, PinDirection.DELAYED_OUTPUT),
                        NOT_Q, new Pin(SignalType.DIGITAL, PinDirection.DELAYED_OUTPUT)
                ),
                false,
                MyCodecs.BOOL,
                new CellCost(5, 4.5)
        );
    }

    @Override
    public Boolean nextState(CircuitSignals inputSignals, Boolean currentState) {
        boolean r = inputSignals.bool(RESET);
        boolean s = inputSignals.bool(SET);
        if (r && s) {
            //TODO?
            return RAND.nextBoolean();
        } else if (r) {
            return false;
        } else if (s) {
            return true;
        } else {
            return currentState;
        }
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Boolean oldState) {
        return CircuitSignals.ofBools(ImmutableMap.of(Q, oldState, NOT_Q, !oldState));
    }
}
