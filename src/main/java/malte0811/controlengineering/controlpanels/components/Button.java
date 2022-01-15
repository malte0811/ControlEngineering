package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.world.InteractionResult;

public class Button extends PanelComponentType<ColorAndSignal, Boolean> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.button";
    public static final Vec2d SIZE = new Vec2d(1, 1);
    public static final double HEIGHT = 0.5;

    public Button() {
        super(ColorAndSignal.DEFAULT, false, ColorAndSignal.CODEC, Codec.BOOL, SIZE, HEIGHT, TRANSLATION_KEY);
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, Boolean active) {
        if (active) {
            return config.signal().singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Pair<InteractionResult, Boolean> click(ColorAndSignal config, Boolean oldState, boolean sneaking) {
        return Pair.of(InteractionResult.SUCCESS, !oldState);
    }
}
