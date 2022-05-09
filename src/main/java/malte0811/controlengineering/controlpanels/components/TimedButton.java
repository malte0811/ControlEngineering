package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.world.InteractionResult;

public class TimedButton extends PanelComponentType<ColorAndSignal, Integer> {
    //TODO config?
    private static final int DELAY = 20;

    public TimedButton() {
        super(ColorAndSignal.DEFAULT, 0, ColorAndSignal.CODEC, MyCodecs.INTEGER, Button.SIZE, Button.HEIGHT);
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, Integer remainingOn) {
        if (isActive(remainingOn)) {
            return config.signal().singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Integer tick(ColorAndSignal config, Integer remainingOn) {
        return remainingOn > 0 ? remainingOn - 1 : 0;
    }

    @Override
    public Pair<InteractionResult, Integer> click(
            ColorAndSignal config, Integer remainingOn, ComponentClickContext ctx
    ) {
        if (isActive(remainingOn)) {
            return Pair.of(InteractionResult.PASS, remainingOn);
        } else {
            return Pair.of(InteractionResult.SUCCESS, DELAY);
        }
    }

    @Override
    public boolean canClientDistinguish(Integer stateA, Integer stateB) {
        return isActive(stateA) != isActive(stateB);
    }

    public static boolean isActive(Integer state) {
        return state > 0;
    }
}
