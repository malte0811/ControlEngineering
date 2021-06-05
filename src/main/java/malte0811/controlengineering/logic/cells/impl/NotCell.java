package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.CellCost;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class NotCell extends StatelessCell {
    private static final String IN_NAME = "in";

    public NotCell() {
        super(
                ImmutableMap.of(IN_NAME, new Pin(SignalType.DIGITAL, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                new CellCost(1, 0.5)
        );
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals) {
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, debool(!bool(inputSignals.getDouble(IN_NAME))));
    }
}
