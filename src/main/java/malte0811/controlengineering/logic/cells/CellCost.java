package malte0811.controlengineering.logic.cells;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;

public record CellCost(double numTubes, double wireLength) {
    public static CellCost matchingIECosts(LogicCircuitOperator operator, int numInputs) {
        return matchingIECosts(operator, numInputs, 0);
    }

    public static CellCost matchingIECosts(LogicCircuitOperator operator, int numInputs, double tubeOffset) {
        final double factor = numInputs / (double) operator.getArgumentCount();
        return new CellCost(factor * operator.getComplexity() + tubeOffset, factor);
    }

    public double getSolderAmount() {
        return (numTubes + wireLength) / 2;
    }
}
