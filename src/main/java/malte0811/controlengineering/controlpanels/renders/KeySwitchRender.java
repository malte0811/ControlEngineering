package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.components.KeySwitch;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Map;

public class KeySwitchRender implements ComponentRenderer<BusSignalRef, KeySwitch.State> {
    private static final int BOX_COLOR = 0xaaaaaa;
    private static final int HOLE_COLOR = 0x222222;
    private static final int KEY_COLOR = 0xcccccc;
    private static final double SLOT_WIDTH = 0.125;
    private static final double SLOT_HEIGHT = 0.625;
    private static final double KEY_BASE_Y = 0.25;
    private static final Map<Direction, Integer> BOX_COLORS = RenderUtils.makeColorsExcept(BOX_COLOR, Direction.DOWN);
    private static final Map<Direction, Integer> KEY_BASE_COLORS = RenderUtils.makeColorsExcept(
            KEY_COLOR, Direction.DOWN, Direction.UP
    );
    private static final Map<Direction, Integer> KEY_HANDLE_COLORS = RenderUtils.makeColorsExcept(0);
    private static final Quaternionf TURN_KEY = new Quaternionf().rotateY(Mth.HALF_PI);

    @Override
    public void render(MixedModel output, BusSignalRef busSignalRef, KeySwitch.State state, PoseStack transform) {
        ComponentRenderer.renderBase(output, transform, KeySwitch.SIZE, HOLE_COLOR);
        if (state.baseState() == KeySwitch.BaseState.EMPTY) {
            return;
        }
        RenderUtils.renderColoredBox(
                output, transform, Vec3.ZERO, KeySwitch.SIZE.withHeight(KeySwitch.HEIGHT),
                BOX_COLORS, Map.of(), RenderUtils.ALL_DYNAMIC
        );
        transform.pushPose();
        transform.translate(0, KeySwitch.HEIGHT, 0);
        if (state.baseState() == KeySwitch.BaseState.KEY_TURNED) {
            transform.translate(KeySwitch.SIZE.x() / 2, 0, KeySwitch.SIZE.y() / 2);
            transform.mulPose(TURN_KEY);
            transform.translate(-KeySwitch.SIZE.x() / 2, 0, -KeySwitch.SIZE.y() / 2);
        }
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        transform.translate(0.5, 0, 0.5);
        var transformed = new TransformingVertexBuilder(output, MixedModel.SOLID_DYNAMIC, transform);
        new QuadBuilder(
                new Vec3(-SLOT_WIDTH / 2, EPSILON, SLOT_HEIGHT / 2),
                new Vec3(SLOT_WIDTH / 2, EPSILON, SLOT_HEIGHT / 2),
                new Vec3(SLOT_WIDTH / 2, EPSILON, -SLOT_HEIGHT / 2),
                new Vec3(-SLOT_WIDTH / 2, EPSILON, -SLOT_HEIGHT / 2)
        ).setRGB(HOLE_COLOR).writeTo(transformed);
        if (state.baseState() != KeySwitch.BaseState.HAS_LOCK) {
            RenderUtils.renderColoredBox(
                    output, transform,
                    new Vec3(-SLOT_WIDTH / 2, 0, -SLOT_HEIGHT / 2.5),
                    new Vec3(SLOT_WIDTH / 2, KEY_BASE_Y, SLOT_HEIGHT / 2.5),
                    KEY_BASE_COLORS, Map.of(), RenderUtils.ALL_DYNAMIC
            );
            RenderUtils.renderColoredBox(
                    output, transform,
                    new Vec3(-SLOT_WIDTH, KEY_BASE_Y, -0.4),
                    new Vec3(SLOT_WIDTH, KEY_BASE_Y + 0.8, 0.4),
                    KEY_HANDLE_COLORS, Map.of(), RenderUtils.ALL_DYNAMIC
            );
        }
        transform.popPose();
    }
}
