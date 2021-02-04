package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class InvertedAssociativeCell extends AssociativeFunctionCell {
    public InvertedAssociativeCell(
            String baseName, int numInputs, IBooleanFunction nonInvertedFunc, boolean baseState
    ) {
        super(baseName, numInputs, nonInvertedFunc, baseState);
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Unit currentState) {
        double nonInverted = super.getOutputSignals(inputSignals, currentState).getDouble(0);
        return DoubleLists.singleton(1 - nonInverted);
    }
}
