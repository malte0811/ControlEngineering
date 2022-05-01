package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.Map;

public class ConfigSwitch extends LeafcellType<Unit, Boolean> {
    public ConfigSwitch() {
        super(
                Map.of(),
                Map.of(DEFAULT_OUT_NAME, new Pin(SignalType.DIGITAL, PinDirection.OUTPUT)),
                Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE),
                false, MyCodecs.BOOL,
                new CellCost(0, 1)
        );
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Unit oldState, Boolean config) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, config);
    }
}
