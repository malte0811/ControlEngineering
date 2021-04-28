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
    public static final String LOW_PIN = "low";
    public static final String HIGH_PIN = "high";

    public SchmittTrigger() {
        super(
                ImmutableMap.of(
                        LOW_PIN, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        HIGH_PIN, new Pin(SignalType.ANALOG, PinDirection.INPUT)
                ),
                ImmutableMap.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                false,
                Codec.BOOL,
                3
        );
    }

    @Override
    public Boolean nextState(Object2DoubleMap<String> inputSignals, Boolean currentState) {
        if (currentState) {
            if (inputSignals.getDouble(DEFAULT_IN_NAME) <= inputSignals.getDouble(LOW_PIN)) {
                return false;
            }
        } else if (inputSignals.getDouble(DEFAULT_IN_NAME) >= inputSignals.getDouble(HIGH_PIN)) {
            return true;
        }
        return currentState;
    }

    @Override
    public Object2DoubleMap<String> getOutputSignals(Object2DoubleMap<String> inputSignals, Boolean oldState) {
        return Object2DoubleMaps.singleton(DEFAULT_OUT_NAME, debool(nextState(inputSignals, oldState)));
    }
}
