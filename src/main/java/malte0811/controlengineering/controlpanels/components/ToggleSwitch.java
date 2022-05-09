package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.world.InteractionResult;

public class ToggleSwitch extends PanelComponentType<BusSignalRef, Boolean> {
    public static final Vec2d SIZE = new Vec2d(1, 2);
    public static final double SELECTION_HEIGHT = 1.5;

    public ToggleSwitch() {
        super(
                BusSignalRef.DEFAULT, false,
                BusSignalRef.CODEC, MyCodecs.BOOL,
                SIZE, SELECTION_HEIGHT
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
    public Pair<InteractionResult, Boolean> click(
            BusSignalRef outputSignal, Boolean oldState, ComponentClickContext ctx
    ) {
        return Pair.of(InteractionResult.SUCCESS, !oldState);
    }
}
