package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;

import java.util.List;

public abstract class StatelessCell extends LeafcellType<Unit> {
    protected StatelessCell(List<Pin> inputPins, List<Pin> outputPins, int numTubes) {
        super(inputPins, outputPins, Unit.INSTANCE, Codec.unit(Unit.INSTANCE), numTubes);
    }

    @Override
    public final Unit nextState(DoubleList inputSignals, Unit currentState) {
        return currentState;
    }

    @Override
    public final DoubleList getOutputSignals(DoubleList inputSignals, Unit oldState) {
        return getOutputSignals(inputSignals);
    }

    protected abstract DoubleList getOutputSignals(DoubleList inputSignals);
}
