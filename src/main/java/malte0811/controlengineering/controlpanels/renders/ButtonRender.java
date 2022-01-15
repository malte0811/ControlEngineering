package malte0811.controlengineering.controlpanels.renders;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.ColorUtils;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

public class ButtonRender<S> extends ComponentRenderer<ColorAndSignal, S> {
    private static final Vec3 BOX_MIN = Vec3.ZERO;
    private static final Vec3 BOX_MAX = new Vec3(1, 0.5, 1);
    private static final Map<Direction, RenderType> TARGETS = ImmutableMap.<Direction, RenderType>builder()
            .put(Direction.NORTH, MixedModel.SOLID_STATIC)
            .put(Direction.EAST, MixedModel.SOLID_STATIC)
            .put(Direction.SOUTH, MixedModel.SOLID_STATIC)
            .put(Direction.WEST, MixedModel.SOLID_STATIC)
            .put(Direction.UP, MixedModel.SOLID_DYNAMIC)
            .build();
    private final Predicate<S> active;

    public ButtonRender(Predicate<S> active) {
        this.active = active;
    }

    @Override
    public void render(MixedModel output, ColorAndSignal config, S state, PoseStack transform) {
        EnumMap<Direction, Integer> colors = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            colors.put(d, -1);
        }
        final Map<Direction, Integer> lightOverrides;
        if (active.test(state)) {
            lightOverrides = ImmutableMap.of(Direction.UP, 15);
            colors.put(Direction.UP, config.color());
        } else {
            lightOverrides = ImmutableMap.of();
            colors.put(Direction.UP, ColorUtils.halfColor(config.color()));
        }
        RenderUtils.renderColoredBox(output, transform, BOX_MIN, BOX_MAX, colors, lightOverrides, TARGETS);
    }
}
