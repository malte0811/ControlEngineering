package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class Digitizer extends StatelessCell {
    public Digitizer() {
        super(
                ImmutableMap.of(DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                new CellCost(1, 1)
        );
    }

    @Override
    protected Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, digitize(inputSignals.getDouble(DEFAULT_IN_NAME)));
    }

    public static double digitize(double in) {
        return debool(in > 0.5);
    }
}
