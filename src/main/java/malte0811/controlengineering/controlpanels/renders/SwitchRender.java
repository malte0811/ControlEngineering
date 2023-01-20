package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.Map;

import static malte0811.controlengineering.client.render.target.RenderUtils.ALL_DYNAMIC;
import static malte0811.controlengineering.controlpanels.components.ToggleSwitch.SIZE;

public class SwitchRender implements ComponentRenderer<BusSignalRef, Boolean> {
    private static final int BASE_COLOR = 0xd0d0d0;
    private static final int ROD_COLOR = 0xa0a0a0;
    private static final Quaternionf ROTATION_OFF = new Quaternionf().rotateX(Mth.HALF_PI / 3);
    private static final Quaternionf ROTATION_ON = new Quaternionf().rotateX(-Mth.HALF_PI / 3);
    private static final Map<Direction, Integer> SIDE_COLORS = Util.make(new EnumMap<>(Direction.class), sideColors -> {
        for (Direction side : DirectionUtils.VALUES) {
            if (side != Direction.DOWN) {
                sideColors.put(side, ROD_COLOR);
            }
        }
    });
    private static final float ROD_DIAMETER = 0.5f;

    @Override
    public void render(MixedModel output, BusSignalRef state, Boolean active, PoseStack transform) {
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        ComponentRenderer.renderBase(output, transform, SIZE, BASE_COLOR);
        transform.pushPose();
        transform.translate((SIZE.x() - ROD_DIAMETER) / 2, 0, (SIZE.y() - ROD_DIAMETER) / 2);
        transform.mulPose(active ? ROTATION_ON : ROTATION_OFF);
        RenderUtils.renderColoredBox(
                output, transform,
                new Vec3(0, -.1, 0), new Vec3(ROD_DIAMETER, 1.5, ROD_DIAMETER),
                SIDE_COLORS, Map.of(), ALL_DYNAMIC
        );
        transform.popPose();
    }
}
