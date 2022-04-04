package malte0811.controlengineering.logic.cells.impl;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.CircuitSignals;
import net.minecraft.world.phys.shapes.BooleanOp;

public class InvertedAssociativeCell extends AssociativeFunctionCell {
    public InvertedAssociativeCell(
            AssociativeFunctionCell nonInverted, LogicCircuitOperator operatorForCost, boolean baseState
    ) {
        this(
                nonInverted.getInputPins().size(),
                (a, b) -> nonInverted.getBaseFunction().apply(a, b),
                baseState,
                CellCost.matchingIECosts(operatorForCost, nonInverted.getInputPins().size())
        );
    }

    public InvertedAssociativeCell(int numInputs, BooleanOp nonInvertedFunc, boolean baseState, CellCost cost) {
        super(numInputs, nonInvertedFunc, baseState, cost);
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals) {
        var nonInverted = super.getOutputSignals(inputSignals).value(DEFAULT_OUT_NAME);
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, BusLine.MAX_VALID_VALUE - nonInverted);
    }
}
