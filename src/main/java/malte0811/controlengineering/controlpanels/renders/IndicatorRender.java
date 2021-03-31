package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.target.RenderTarget;
import malte0811.controlengineering.client.render.target.TargetType;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.util.math.vector.Vector3d;

public class IndicatorRender extends ComponentRenderer<Indicator> {
    @Override
    public void render(RenderTarget output, Indicator instance, MatrixStack transform) {
        double colorFactor = 0.5 + instance.getRsValue() / (2. * BusLine.MAX_VALID_VALUE);
        int ownBrightness = (instance.getRsValue() * 15) / BusLine.MAX_VALID_VALUE;
        new QuadBuilder(Vector3d.ZERO, new Vector3d(0, 0, 1), new Vector3d(1, 0, 1), new Vector3d(1, 0, 0))
                .setNormal(new Vector3d(0, 1, 0))
                .setRGB(ColorUtils.fractionalColor(instance.getColor(), colorFactor))
                .setBlockLightOverride(ownBrightness)
                .writeTo(transform, output, TargetType.DYNAMIC);
    }
}
