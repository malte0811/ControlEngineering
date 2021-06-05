package malte0811.controlengineering.logic.cells.impl;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.CellCost;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class InvertedAssociativeCell extends AssociativeFunctionCell {
    public InvertedAssociativeCell(
            int numInputs, LogicCircuitOperator nonInvertedFunc, boolean baseState
    ) {
        this(
                numInputs,
                (a, b) -> nonInvertedFunc.apply(new boolean[]{a, b}),
                baseState,
                CellCost.matchingIECosts(nonInvertedFunc, numInputs, -0.5)
        );
    }

    public InvertedAssociativeCell(int numInputs, IBooleanFunction nonInvertedFunc, boolean baseState, CellCost cost) {
        super(numInputs, nonInvertedFunc, baseState, cost);
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        double nonInverted = super.getOutputSignals(inputSignals).getDouble(DEFAULT_OUT_NAME);
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, 1 - nonInverted);
    }
}
