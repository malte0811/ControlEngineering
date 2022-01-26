package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;

import java.util.Map;

public abstract class StatelessCell extends LeafcellType<Unit> {
    protected StatelessCell(Map<String, Pin> inputPins, Map<String, Pin> outputPins, CellCost cost) {
        super(inputPins, outputPins, Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE), cost);
    }

    @Override
    public final Unit nextState(Object2DoubleMap<String> inputSignals, Unit currentState) {
        return currentState;
    }

    @Override
    public final Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, Unit oldState) {
        return getOutputSignals(inputSignals);
    }

    protected abstract Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals);
}
