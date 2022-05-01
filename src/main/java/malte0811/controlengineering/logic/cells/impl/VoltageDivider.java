package malte0811.controlengineering.logic.cells.impl;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.util.Unit;

import java.util.Map;

public class VoltageDivider extends LeafcellType<Unit, Integer> {
    public static final int TOTAL_RESISTANCE = 100;
    public static final MyCodec<Integer> RESISTANCE_CODEC = MyCodecs.INTEGER.copy();
    public static final String RESISTANCE_KEY = ControlEngineering.MODID + ".gui.lower_resistance";

    public static final String INPUT_TOP = "input_a";
    public static final String INPUT_BOTTOM = "input_b";

    public VoltageDivider() {
        super(
                Map.of(
                        INPUT_TOP, new Pin(SignalType.ANALOG, PinDirection.INPUT),
                        INPUT_BOTTOM, new Pin(SignalType.ANALOG, PinDirection.INPUT)
                ),
                Map.of(DEFAULT_OUT_NAME, new Pin(SignalType.ANALOG, PinDirection.OUTPUT)),
                Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE),
                TOTAL_RESISTANCE / 2, RESISTANCE_CODEC,
                new CellCost(0, 4)
        );
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Unit oldState, Integer ratio) {
        final var top = inputSignals.value(INPUT_TOP);
        final var bottom = inputSignals.value(INPUT_BOTTOM);
        final var out = (ratio * top + (TOTAL_RESISTANCE - ratio) * bottom) / TOTAL_RESISTANCE;
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, out);
    }
}
