package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.world.InteractionResult;

public class ToggleSwitch extends PanelComponentType<BusSignalRef, Boolean> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.switch";
    public static final Vec2d SIZE = new Vec2d(1, 2);
    public static final double SELECTION_HEIGHT = 1.5;

    public ToggleSwitch() {
        super(
                BusSignalRef.DEFAULT, false,
                BusSignalRef.CODEC, Codec.BOOL,
                SIZE, SELECTION_HEIGHT, TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(BusSignalRef outputSignal, Boolean state) {
        if (state) {
            return outputSignal.singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Boolean updateTotalState(BusSignalRef outputSignal, Boolean oldState, BusState busState) {
        return oldState;
    }

    @Override
    public Boolean tick(BusSignalRef outputSignal, Boolean oldState) {
        return oldState;
    }

    @Override
    public Pair<InteractionResult, Boolean> click(BusSignalRef outputSignal, Boolean oldState, boolean sneaking) {
        return Pair.of(InteractionResult.SUCCESS, !oldState);
    }
}
