package malte0811.controlengineering.controlpanels.renders;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.controlpanels.components.Button;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumMap;

public class ButtonRender extends ComponentRenderer<Button> {
    private static final Vector3d BOX_MIN = Vector3d.ZERO;
    private static final Vector3d BOX_MAX = new Vector3d(1, 0.5, 1);

    @Override
    public void render(
            IVertexBuilder builder,
            Button instance,
            MatrixStack transform,
            int packedLightIn,
            int packedOverlayIn
    ) {
        RenderHelper helper = new RenderHelper(builder, packedLightIn, packedOverlayIn);
        EnumMap<Direction, Integer> colors = new EnumMap<>(Direction.class);
        for (Direction d : Direction.BY_HORIZONTAL_INDEX) {
            colors.put(d, -1);
        }
        colors.put(Direction.UP, instance.color);
        helper.renderColoredBox(transform, BOX_MIN, BOX_MAX, colors, ImmutableMap.of(Direction.UP, FULLBRIGHT));
    }
}
