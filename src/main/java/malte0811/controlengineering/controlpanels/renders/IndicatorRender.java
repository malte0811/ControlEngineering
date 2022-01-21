package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.world.phys.Vec3;

public class IndicatorRender implements ComponentRenderer<ColorAndSignal, Integer> {
    @Override
    public void render(MixedModel output, ColorAndSignal config, Integer strength, PoseStack transform) {
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        TransformingVertexBuilder builder = new TransformingVertexBuilder(output, MixedModel.SOLID_DYNAMIC, transform);
        new QuadBuilder(
                new Vec3(0, EPSILON, 0),
                new Vec3(0, EPSILON, 1),
                new Vec3(1, EPSILON, 1),
                new Vec3(1, EPSILON, 0)
        ).setNormal(new Vec3(0, 1, 0))
                .setRGB(colorForStrength(config.color(), strength))
                .setBlockLightOverride(lightForStrength(strength))
                .writeTo(builder);
    }

    public static int colorForStrength(int baseColor, int strength) {
        double colorFactor = 0.5 + strength / (2. * BusLine.MAX_VALID_VALUE);
        return ColorUtils.fractionalColor(baseColor, colorFactor);
    }

    public static int lightForStrength(int strength) {
        return (strength * 15) / BusLine.MAX_VALID_VALUE;
    }
}
