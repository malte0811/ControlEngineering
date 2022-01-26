package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

public class Button extends PanelComponentType<ColorAndSignal, Boolean> {
    public static final Vec2d SIZE = new Vec2d(1, 1);
    public static final double HEIGHT = 0.5;

    public Button() {
        super(ColorAndSignal.DEFAULT, false, ColorAndSignal.CODEC, MyCodecs.BOOL, SIZE, HEIGHT);
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
    public Pair<InteractionResult, Boolean> click(
            ColorAndSignal config, Boolean oldState, boolean sneaking, Vec3 relativeHit
    ) {
        return Pair.of(InteractionResult.SUCCESS, !oldState);
    }
}
