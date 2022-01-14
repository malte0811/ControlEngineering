package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.components.CoveredToggleSwitch.State;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class CoveredSwitchRender extends ComponentRenderer<ColorAndSignal, State> {
    private static final Vec3 SIZE = Util.make(() -> {
        var shape = Objects.requireNonNull(PanelComponents.COVERED_SWITCH.getSelectionShape());
        return new Vec3(shape.maxX, shape.maxY, shape.maxZ);
    });
    private static final Quaternion OPEN_ROTATION = new Quaternion(-60, 0, 0, true);

    @Override
    public void render(MixedModel output, ColorAndSignal config, State state, PoseStack transform) {
        var switchRender = ComponentRenderers.getRenderer(PanelComponents.TOGGLE_SWITCH);
        switchRender.render(output, config.signal(), state == State.ACTIVE, transform);
        if (state != State.CLOSED) {
            transform.pushPose();
            transform.mulPose(OPEN_ROTATION);
        }
        Map<Direction, Integer> colors = new EnumMap<>(Direction.class);
        colors.put(Direction.NORTH, config.color());
        colors.put(Direction.UP, config.color());
        colors.put(Direction.EAST, config.color());
        colors.put(Direction.WEST, config.color());
        RenderUtils.renderColoredBox(output, transform, Vec3.ZERO, SIZE, colors, Map.of(), RenderUtils.ALL_DYNAMIC);
        RenderUtils.renderColoredBox(
                output, transform, Vec3.ZERO, SIZE, colors, Map.of(), RenderUtils.ALL_DYNAMIC, true
        );
        if (state != State.CLOSED) {
            transform.popPose();
        }
    }
}
