package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class NotCell extends StatelessCell {
    public NotCell() {
        super(
                ImmutableList.of(new Pin("in", SignalType.DIGITAL, PinDirection.INPUT)),
                ImmutableList.of(new Pin("out", SignalType.DIGITAL, PinDirection.OUTPUT)),
                1
        );
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals) {
        return DoubleLists.singleton(bool(inputSignals.getDouble(0)) ? 0 : 1);
    }
}
