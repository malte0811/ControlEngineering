package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class AssociativeFunctionCell extends StatelessCell {
    private final DoubleBiFunction func;
    private final double baseState;

    public AssociativeFunctionCell(int numInputs, IBooleanFunction func, boolean baseState, int numTubes) {
        this(numInputs, (a, b) -> func.apply(bool(a), bool(b)) ? 1 : 0, baseState ? 1 : 0, numTubes);
    }

    public AssociativeFunctionCell(int numInputs, DoubleBiFunction func, double baseState, int numTubes) {
        super(
                Pin.numbered(numInputs, "in", SignalType.DIGITAL, PinDirection.INPUT),
                ImmutableList.of(new Pin("out", SignalType.DIGITAL, PinDirection.OUTPUT)),
                numTubes
        );
        this.func = func;
        this.baseState = baseState;
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals) {
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
