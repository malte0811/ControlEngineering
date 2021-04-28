package malte0811.controlengineering.logic.cells.impl;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class InvertedAssociativeCell extends AssociativeFunctionCell {
    public InvertedAssociativeCell(int numInputs, IBooleanFunction nonInvertedFunc, boolean baseState, int numTubes) {
        super(numInputs, nonInvertedFunc, baseState, numTubes);
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        double nonInverted = super.getOutputSignals(inputSignals).getDouble(DEFAULT_OUT_NAME);
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, 1 - nonInverted);
    }
}
