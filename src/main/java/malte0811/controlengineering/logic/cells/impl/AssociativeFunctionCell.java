package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class AssociativeFunctionCell extends StatelessCell {
    private final DoubleBiFunction func;
    private final double baseState;

    public AssociativeFunctionCell(int numInputs, IBooleanFunction func, boolean baseState, int numTubes) {
        this(numInputs, (a, b) -> debool(func.apply(bool(a), bool(b))), debool(baseState), numTubes);
    }

    public AssociativeFunctionCell(int numInputs, DoubleBiFunction func, double baseState, int numTubes) {
        super(
                Pin.numbered(numInputs, "in", SignalType.DIGITAL, PinDirection.INPUT),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                numTubes
        );
        this.func = func;
        this.baseState = baseState;
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        double result = baseState;
        for (double d : inputSignals.values()) {
            result = func.apply(result, d);
        }
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, result);
    }

    public interface DoubleBiFunction {
        double apply(double a, double b);
    }
}
