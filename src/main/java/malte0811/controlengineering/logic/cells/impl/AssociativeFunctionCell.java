package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Unit;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.IBooleanFunction;

public class AssociativeFunctionCell extends StatelessCell {
    private final DoubleBiFunction func;
    private final double baseState;

    public AssociativeFunctionCell(String baseName, int numInputs, IBooleanFunction func, boolean baseState) {
        this(baseName, numInputs, (a, b) -> func.apply(bool(a), bool(b)) ? 1 : 0, baseState ? 1 : 0);
    }

    public AssociativeFunctionCell(String baseName, int numInputs, DoubleBiFunction func, double baseState) {
        super(
                new ResourceLocation(ControlEngineering.MODID, baseName + numInputs),
                Pin.numbered(numInputs, "I", SignalType.DIGITAL),
                ImmutableList.of(new Pin("O", SignalType.DIGITAL))
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
