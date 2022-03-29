package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

public class Slider extends PanelComponentType<ColorAndSignal, Integer> {
    public static final double KNOB_SIZE = 1;
    public static final double LENGTH = 8;
    private static final Vec2d SIZE_VERT = new Vec2d(KNOB_SIZE, LENGTH);
    private static final Vec2d SIZE_HOR = new Vec2d(LENGTH, KNOB_SIZE);
    public static final double KNOB_HEIGHT = Button.HEIGHT;
    public static final double MIN_CENTER = LENGTH - KNOB_SIZE / 2;
    public static final double MAX_CENTER = KNOB_SIZE / 2;
    private final boolean horizontal;

    public Slider(boolean horizontal) {
        super(ColorAndSignal.DEFAULT, 0, ColorAndSignal.CODEC, MyCodecs.INTEGER, getSize(horizontal), KNOB_HEIGHT);
        this.horizontal = horizontal;
    }

    @Override
    public Pair<InteractionResult, Integer> click(
            ColorAndSignal config, Integer oldState, boolean sneaking, Vec3 relativeHit
    ) {
        var hitCoord = horizontal ? LENGTH - relativeHit.x : relativeHit.z;
        var relativeStrength = Mth.clamp(Mth.inverseLerp(hitCoord, MIN_CENTER, MAX_CENTER), 0, 1);
        var strength = (int) Math.round(relativeStrength * BusLine.MAX_VALID_VALUE);
        return Pair.of(InteractionResult.SUCCESS, strength);
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, Integer strength) {
        return config.signal().singleSignalState(strength);
    }

    public static Vec2d getSize(boolean horizontal) {
        return horizontal ? SIZE_HOR : SIZE_VERT;
    }
}
