package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;

public class NotCell extends StatelessCell {
    public NotCell() {
        super(
                ImmutableList.of(new Pin("in", SignalType.DIGITAL)),
                ImmutableList.of(new Pin("out", SignalType.DIGITAL)),
                0.5
        );
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Unit currentState) {
        return DoubleLists.singleton(bool(inputSignals.getDouble(0)) ? 0 : 1);
    }
}
