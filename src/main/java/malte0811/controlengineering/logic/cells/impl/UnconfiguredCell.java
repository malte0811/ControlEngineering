package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.CircuitSignals;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.Map;

public abstract class UnconfiguredCell<State> extends LeafcellType<State, Unit> {
    protected UnconfiguredCell(
            Map<String, Pin> inputPins, Map<String, Pin> outputPins,
            State initialState, MyCodec<State> stateCodec,
            CellCost cost
    ) {
        super(
                inputPins, outputPins,
                initialState, Unit.INSTANCE,
                // Use mapped state codec to stay compatible with older circuits
                stateCodec.xmap(s -> Pair.of(s, Unit.INSTANCE), Pair::getFirst), MyCodecs.unit(Unit.INSTANCE),
                cost
        );
    }

    @Override
    public final CircuitSignals getOutputSignals(CircuitSignals inputSignals, State oldState, Unit unit) {
        return getOutputSignals(inputSignals, oldState);
    }

    public abstract CircuitSignals getOutputSignals(CircuitSignals inputSignals, State oldState);

    @Override
    public final State nextState(CircuitSignals inputSignals, State currentState, Unit unit) {
        return nextState(inputSignals, currentState);
    }

    public abstract State nextState(CircuitSignals inputSignals, State currentState);
}
