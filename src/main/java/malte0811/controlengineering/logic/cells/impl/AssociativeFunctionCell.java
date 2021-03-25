package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class AssociativeFunctionCell extends StatelessCell {
    private final DoubleBiFunction func;
    private final double baseState;

    public AssociativeFunctionCell(int numInputs, IBooleanFunction func, boolean baseState, double numTubes) {
        this(numInputs, (a, b) -> func.apply(bool(a), bool(b)) ? 1 : 0, baseState ? 1 : 0, numTubes);
    }

    public AssociativeFunctionCell(int numInputs, DoubleBiFunction func, double baseState, double numTubes) {
        super(
                Pin.numbered(numInputs, "I", SignalType.DIGITAL),
                ImmutableList.of(new Pin("O", SignalType.DIGITAL)),
                numTubes
        );
        this.func = func;
        this.baseState = baseState;
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Unit currentState) {
        double result = baseState;
        for (double d : inputSignals) {
            result = func.apply(result, d);
        }
        return DoubleLists.singleton(result);
    }

    public interface DoubleBiFunction {
        double apply(double a, double b);
    }
}
