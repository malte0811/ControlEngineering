package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;

public class SchmittTrigger extends LeafcellType<Boolean> {
    private static final double TRIGGER_LOW = 0.4;
    private static final double TRIGGER_HIGH = 0.6;

    public SchmittTrigger() {
        super(
                ImmutableList.of(new Pin("in", SignalType.ANALOG)),
                ImmutableList.of(new Pin("out", SignalType.DIGITAL)),
                false,
                Codec.BOOL,
                2
        );
    }

    @Override
    public Boolean nextState(DoubleList inputSignals, Boolean currentState) {
        if (currentState) {
            if (inputSignals.getDouble(0) < TRIGGER_LOW) {
                return false;
            }
        } else if (inputSignals.getDouble(0) > TRIGGER_HIGH) {
            return true;
        }
        return currentState;
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Boolean currentState) {
        return DoubleLists.singleton(nextState(inputSignals, currentState) ? 1 : 0);
    }
}
