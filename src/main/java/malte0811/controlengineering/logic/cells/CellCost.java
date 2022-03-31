package malte0811.controlengineering.logic.cells;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;

public record CellCost(double numTubes, double wireLength) {
    public static final CellCost FOR_FREE = new CellCost(0, 0);

    public static CellCost matchingIECosts(LogicCircuitOperator operator, int numInputs) {
        final double factor = numInputs / (double) operator.getArgumentCount();
        return new CellCost(factor * operator.getComplexity(), factor);
    }

    public double getSolderAmount() {
        return (numTubes + wireLength) / 2;
    }
}
