package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.world.phys.Vec3;

public class IndicatorRender extends ComponentRenderer<ColorAndSignal, Integer> {
    @Override
    public void render(MixedModel output, ColorAndSignal config, Integer rsValue, PoseStack transform) {
        double colorFactor = 0.5 + rsValue / (2. * BusLine.MAX_VALID_VALUE);
        int ownBrightness = (rsValue * 15) / BusLine.MAX_VALID_VALUE;
        final double zOffset = 1e-3;
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        TransformingVertexBuilder builder = new TransformingVertexBuilder(output, MixedModel.SOLID_DYNAMIC, transform);
        new QuadBuilder(
                new Vec3(0, zOffset, 0),
                new Vec3(0, zOffset, 1),
                new Vec3(1, zOffset, 1),
                new Vec3(1, zOffset, 0)
        ).setNormal(new Vec3(0, 1, 0))
                .setRGB(ColorUtils.fractionalColor(config.color(), colorFactor))
                .setBlockLightOverride(ownBrightness)
                .writeTo(builder);
    }
}
