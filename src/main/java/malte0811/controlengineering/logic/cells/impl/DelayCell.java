package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class DelayCell extends LeafcellType<Double> {
    public DelayCell(SignalType type, int numTubes) {
        super(
                ImmutableList.of(new Pin("in", type, PinDirection.INPUT)),
                ImmutableList.of(new Pin("out", type, PinDirection.DELAYED_OUTPUT)),
                0D, Codec.DOUBLE, numTubes
        );
    }

    @Override
    public Double nextState(DoubleList inputSignals, Double currentState) {
        return inputSignals.getDouble(0);
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Double oldState) {
        return DoubleLists.singleton(oldState);
    }
}
