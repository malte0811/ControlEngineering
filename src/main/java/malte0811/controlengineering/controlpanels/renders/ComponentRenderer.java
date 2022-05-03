package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.world.phys.Vec3;

public interface ComponentRenderer<Config, State> {
    double EPSILON = 1e-2;

    void render(MixedModel output, Config config, State state, PoseStack transform);

    static void renderBase(MixedModel output, PoseStack transform, Vec2d size, int color) {
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        var transformedStatic = new TransformingVertexBuilder(output, MixedModel.SOLID_STATIC, transform);
        new QuadBuilder(
                new Vec3(0, EPSILON, size.y()),
                new Vec3(size.x(), EPSILON, size.y()),
                new Vec3(size.x(), EPSILON, 0),
                new Vec3(0, EPSILON, 0)
        ).setRGB(color).writeTo(transformedStatic);
    }
}
