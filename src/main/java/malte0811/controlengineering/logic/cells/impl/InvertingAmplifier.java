package malte0811.controlengineering.logic.cells.impl;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.*;
import malte0811.controlengineering.util.math.Fraction;
import malte0811.controlengineering.util.mycodec.MyCodecs;

import java.util.Map;

public class InvertingAmplifier extends LeafcellType<Unit, Fraction> {
    public static final String AMPLIFY_BY = ControlEngineering.MODID + ".gui.amp.amplify";
    public static final String ATTENUATE_BY = ControlEngineering.MODID + ".gui.amp.attenuate";

    public InvertingAmplifier() {
        super(
                Map.of(DEFAULT_IN_NAME, new Pin(SignalType.ANALOG, PinDirection.INPUT)),
                Map.of(DEFAULT_OUT_NAME, new Pin(SignalType.ANALOG, PinDirection.OUTPUT)),
                Unit.INSTANCE, MyCodecs.unit(Unit.INSTANCE),
                Fraction.ONE, Fraction.CODEC,
                new CellCost(3, 3)
        );
    }

    @Override
    public Unit nextState(CircuitSignals inputSignals, Unit currentState, Fraction scalingFactor) {
        return currentState;
    }

    @Override
    public CircuitSignals getOutputSignals(CircuitSignals inputSignals, Unit oldState, Fraction scalingFactor) {
        return CircuitSignals.singleton(DEFAULT_OUT_NAME, -scalingFactor.apply(inputSignals.value(DEFAULT_IN_NAME)));
    }
}
