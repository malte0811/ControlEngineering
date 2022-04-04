package malte0811.controlengineering.logic.cells.impl;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler;
import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.logic.cells.*;
import net.minecraft.world.phys.shapes.BooleanOp;

public class AssociativeFunctionCell extends StatelessCell {
    private final BooleanOp func;
    private final boolean baseState;

    public AssociativeFunctionCell(int numInputs, LogicCircuitHandler.LogicCircuitOperator func, boolean baseState) {
        this(
                numInputs,
                (a, b) -> func.apply(new boolean[]{a, b}),
                baseState,
                CellCost.matchingIECosts(func, numInputs)
        );
    }

    public AssociativeFunctionCell(int numInputs, BooleanOp func, boolean baseState, CellCost cost) {
        super(
                Pin.numbered(numInputs, "in", SignalType.DIGITAL, PinDirection.INPUT),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                cost
        );
        this.func = func;
        this.baseState = baseState;
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        boolean result = baseState;
        var numTrue = inputSignals.numTrue();
        for (int i = 0; i < inputSignals.size(); ++i) {
            result = func.apply(result, i < numTrue);
        }
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, result);
    }

    public BooleanOp getBaseFunction() {
        return func;
    }
}
