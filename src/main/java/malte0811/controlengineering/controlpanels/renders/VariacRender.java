package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.components.Variac;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

import static malte0811.controlengineering.controlpanels.components.Variac.SIZE;

public class VariacRender implements ComponentRenderer<BusSignalRef, Integer> {
    private static final Map<Direction, Integer> BASE_COLORS = Util.make(new EnumMap<>(Direction.class), map -> {
        final int baseColor = 0xf0f0f0;
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            map.put(d, baseColor);
        }
    });
    private static final Map<Direction, Integer> KNOB_COLORS = Util.make(new EnumMap<>(Direction.class), map -> {
        final int baseColor = 0x202020;
        for (Direction d : DirectionUtils.VALUES) {
            map.put(d, baseColor);
        }
    });
    private static final double ROD_RADIUS = .5;
    private static final double ROD_LENGTH = 1;
    private static final double KNOB_RADIUS = SIZE.x() / (2 * Mth.SQRT_OF_TWO);
    private static final double KNOB_HEIGHT = 1;
    private static final double MAX_Y = ROD_LENGTH + KNOB_HEIGHT;
    private static final Vec3 ROD_MIN = new Vec3(-ROD_RADIUS, 0, -ROD_RADIUS);
    private static final Vec3 ROD_MAX = new Vec3(ROD_RADIUS, ROD_LENGTH, ROD_RADIUS);
    private static final Vec3 KNOB_MIN = new Vec3(-KNOB_RADIUS, ROD_LENGTH, -KNOB_RADIUS);
    private static final Vec3 KNOB_MAX = new Vec3(KNOB_RADIUS, MAX_Y, KNOB_RADIUS);
    private static final Quaternionf ONE_EIGHTH = new Quaternionf()
            .rotateY(Mth.HALF_PI / 2);

    @Override
    public void render(MixedModel output, BusSignalRef busSignalRef, Integer strength, PoseStack transform) {
        transform.pushPose();
        transform.translate(SIZE.x() / 2, 0, SIZE.y() / 2);
        RenderUtils.renderColoredBox(
                output, transform, ROD_MIN, ROD_MAX, BASE_COLORS, Map.of(), RenderUtils.ALL_STATIC
        );
        transform.mulPose(getRotation(strength));
        RenderUtils.renderColoredBox(
                output, transform, KNOB_MIN, KNOB_MAX, KNOB_COLORS, Map.of(), RenderUtils.ALL_DYNAMIC
        );
        transform.pushPose();
        transform.mulPose(ONE_EIGHTH);
        RenderUtils.renderColoredBox(
                output, transform, KNOB_MIN, KNOB_MAX, KNOB_COLORS, Map.of(), RenderUtils.ALL_DYNAMIC
        );
        transform.popPose();
        new QuadBuilder(
                new Vec3(0, MAX_Y + EPSILON, -SIZE.y() / 2),
                new Vec3(0, MAX_Y + EPSILON, -SIZE.y() / 2),
                new Vec3(-0.2, MAX_Y + EPSILON, -SIZE.y() / 2 + 0.4),
                new Vec3(0.2, MAX_Y + EPSILON, -SIZE.y() / 2 + 0.4)
        ).setRGB(-1).writeTo(new TransformingVertexBuilder(output, MixedModel.SOLID_DYNAMIC, transform));
        transform.popPose();
    }

    private static Quaternionf getRotation(int strength) {
        return new Quaternionf().rotateY(Variac.getRotationForStrength(strength));
    }
}
