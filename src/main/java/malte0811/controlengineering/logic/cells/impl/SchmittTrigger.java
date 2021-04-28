package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.PinDirection;
import malte0811.controlengineering.logic.cells.SignalType;

public class SchmittTrigger extends LeafcellType<Boolean> {
    private static final double TRIGGER_LOW = 0.4;
    private static final double TRIGGER_HIGH = 0.6;

    public SchmittTrigger() {
        super(
                ImmutableMap.of(DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT)),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                false,
                Codec.BOOL,
                2
        );
    }

    @Override
    public Boolean nextState(Object2DoubleMap<String> inputSignals, Boolean currentState) {
        if (currentState) {
            if (inputSignals.getDouble(DEFAULT_IN_NAME) < TRIGGER_LOW) {
                return false;
            }
        } else if (inputSignals.getDouble(DEFAULT_IN_NAME) > TRIGGER_HIGH) {
            return true;
        }
        return currentState;
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, Boolean oldState) {
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, nextState(inputSignals, oldState) ? 1 : 0);
    }
}
