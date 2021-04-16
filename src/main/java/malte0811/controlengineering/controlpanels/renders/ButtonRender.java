package malte0811.controlengineering.controlpanels.renders;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.RenderTarget;
import malte0811.controlengineering.client.render.target.TargetType;
import malte0811.controlengineering.controlpanels.components.ColorAndSignal;
import malte0811.controlengineering.util.ColorUtils;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumMap;
import java.util.Map;

public class ButtonRender extends ComponentRenderer<ColorAndSignal, Boolean> {
    private static final Vector3d BOX_MIN = Vector3d.ZERO;
    private static final Vector3d BOX_MAX = new Vector3d(1, 0.5, 1);
    private static final Map<Direction, TargetType> TARGETS = ImmutableMap.<Direction, TargetType>builder()
            .put(Direction.NORTH, TargetType.STATIC)
            .put(Direction.EAST, TargetType.STATIC)
            .put(Direction.SOUTH, TargetType.STATIC)
            .put(Direction.WEST, TargetType.STATIC)
            .put(Direction.UP, TargetType.DYNAMIC)
            .build();

    @Override
    public void render(RenderTarget output, ColorAndSignal config, Boolean active, MatrixStack transform) {
        EnumMap<Direction, Integer> colors = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            colors.put(d, -1);
        }
        final Map<Direction, Integer> lightOverrides;
        if (active) {
            lightOverrides = ImmutableMap.of(Direction.UP, 15);
            colors.put(Direction.UP, config.getColor());
        } else {
            lightOverrides = ImmutableMap.of();
            colors.put(Direction.UP, ColorUtils.halfColor(config.getColor()));
        }
        output.renderColoredBox(transform, BOX_MIN, BOX_MAX, colors, lightOverrides, TARGETS);
    }
}
