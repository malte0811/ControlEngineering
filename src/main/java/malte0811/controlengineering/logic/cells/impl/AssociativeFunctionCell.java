package malte0811.controlengineering.logic.cells.impl;

import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.world.phys.shapes.BooleanOp;

public class AssociativeFunctionCell extends StatelessCell {
    private final DoubleBiFunction func;
    private final double baseState;

    public AssociativeFunctionCell(int numInputs, LogicCircuitHandler.LogicCircuitOperator func, boolean baseState) {
        this(
                numInputs,
                (a, b) -> func.apply(new boolean[]{a, b}),
                baseState,
                CellCost.matchingIECosts(func, numInputs)
        );
    }

    public AssociativeFunctionCell(int numInputs, BooleanOp func, boolean baseState, CellCost cost) {
        this(numInputs, (a, b) -> debool(func.apply(bool(a), bool(b))), debool(baseState), cost);
    }

    public AssociativeFunctionCell(int numInputs, DoubleBiFunction func, double baseState, CellCost cost) {
        super(
                Pin.numbered(numInputs, "in", SignalType.DIGITAL, PinDirection.INPUT),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                cost
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
