package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.CircuitSignals;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.Map;

public abstract class StatelessCell extends LeafcellType<Unit> {
    protected StatelessCell(Map<String, Pin> inputPins, Map<String, Pin> outputPins, CellCost cost) {
        super(inputPins, outputPins, Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE), cost);
    }

    @Override
    public final Unit nextState(CircuitSignals inputSignals, Unit currentState) {
        return currentState;
    }

    @Override
    public final CircuitSignals getOutputSignals(CircuitSignals inputSignals, Unit oldState) {
        return getOutputSignals(inputSignals);
    }

    protected abstract CircuitSignals getOutputSignals(CircuitSignals inputSignals);
}
