package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.logic.cells.*;

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
                Codec.BOOL,
                new CellCost(5, 4.5)
        );
    }

    @Override
    public Boolean nextState(Object2DoubleMap<String> inputSignals, Boolean currentState) {
        boolean r = bool(inputSignals.getDouble(RESET));
        boolean s = bool(inputSignals.getDouble(SET));
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
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, Boolean oldState) {
        final double q = oldState ? 1 : 0;
        return new Object2DoubleArrayMap<>(ImmutableMap.of(Q, q, NOT_Q, 1 - q));
    }
}
