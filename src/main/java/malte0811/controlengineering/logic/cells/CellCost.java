package malte0811.controlengineering.logic.cells;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitOperator;

public class CellCost {
    private final double numTubes;
    private final double wireLength;

    public CellCost(double numTubes, double wireLength) {
        this.numTubes = numTubes;
        this.wireLength = wireLength;
    }

    public static CellCost matchingIECosts(LogicCircuitOperator operator, int numInputs) {
        return matchingIECosts(operator, numInputs, 0);
    }

    public static CellCost matchingIECosts(LogicCircuitOperator operator, int numInputs, double tubeOffset) {
        final double factor = numInputs / (double) operator.getArgumentCount();
        return new CellCost(factor * operator.getComplexity() + tubeOffset, factor);
    }

    public double getNumTubes() {
        return numTubes;
    }

    public double getWireLength() {
        return wireLength;
    }

    public double getSolderAmount() {
        return (numTubes + wireLength) / 2;
    }
}
