package malte0811.controlengineering.controlpanels.components;


import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

public class Variac extends PanelComponentType<BusSignalRef, Integer> {
    public static final Vec2d SIZE = new Vec2d(4, 4);
    public static final float ANGLE_MAX = -Mth.PI + 0.1f * Mth.PI;
    public static final float ANGLE_MIN = Mth.PI - 0.1f * Mth.PI;

    public Variac() {
        super(BusSignalRef.DEFAULT, 0, BusSignalRef.CODEC, MyCodecs.INTEGER, SIZE, 2);
    }

    @Override
    public Pair<InteractionResult, Integer> click(
            BusSignalRef line, Integer oldState, boolean sneaking, Vec3 relativeHit
    ) {
        var xRelativeCenter = relativeHit.x - SIZE.x() / 2;
        var yRelativeCenter = relativeHit.z - SIZE.y() / 2;
        var angle = Math.atan2(-xRelativeCenter, -yRelativeCenter);
        return Pair.of(
                InteractionResult.SUCCESS,
                Mth.clamp(getStrengthForRotation(angle), BusLine.MIN_VALID_VALUE, BusLine.MAX_VALID_VALUE)
        );
    }

    @Override
    public BusState getEmittedState(BusSignalRef line, Integer strength) {
        return line.singleSignalState(strength);
    }

    public static float getRotationForStrength(int strength) {
        var relative = strength / (float) BusLine.MAX_VALID_VALUE;
        return Mth.lerp(relative, ANGLE_MIN, ANGLE_MAX);
    }

    public static int getStrengthForRotation(double rotation) {
        var relative = (float) Mth.inverseLerp(rotation, ANGLE_MIN, ANGLE_MAX);
        return (int) (relative * BusLine.MAX_VALID_VALUE);
    }
}
