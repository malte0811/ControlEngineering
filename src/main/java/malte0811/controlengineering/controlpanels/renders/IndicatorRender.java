package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.util.math.vector.Vector3d;

import java.util.OptionalInt;

public class IndicatorRender extends ComponentRenderer<Indicator> {
    @Override
    public void render(
            IVertexBuilder builder, Indicator instance, MatrixStack transform, int packedLightIn, int packedOverlayIn
    ) {
        RenderHelper helper = new RenderHelper(builder, packedLightIn, packedOverlayIn);
        double colorFactor = 0.5 + instance.getRsValue() / (2. * BusLine.MAX_VALID_VALUE);
        helper.renderColoredQuad(
                transform,
                Vector3d.ZERO,
                new Vector3d(0, 0, 1),
                new Vector3d(1, 0, 1),
                new Vector3d(1, 0, 0),
                new Vector3d(0, 1, 0),
                ColorUtils.fractionalColor(instance.getColor(), colorFactor),
                OptionalInt.empty()
        );
    }
}
