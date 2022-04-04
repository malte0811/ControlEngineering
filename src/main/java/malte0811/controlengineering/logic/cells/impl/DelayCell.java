package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class DelayCell extends LeafcellType<Integer> {
    public DelayCell(SignalType type, double numTubes, double wireLength) {
        super(
                ImmutableMap.of(DEFAULT_IN_NAME, new Pin(type, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(type, PinDirection.DELAYED_OUTPUT)),
                0, MyCodecs.INTEGER, new CellCost(numTubes, wireLength)
        );
    }

    @Override
    public Integer nextState(CircuitSignals inputSignals, Integer currentState) {
        return inputSignals.value(DEFAULT_IN_NAME);
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Integer oldState) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, oldState);
    }
}
