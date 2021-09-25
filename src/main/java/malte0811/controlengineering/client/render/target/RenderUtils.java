package malte0811.controlengineering.client.render.target;

import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;

public class RenderUtils {
    public static void renderColoredBox(
            MixedModel output,
            PoseStack transform, Vec3 min, Vec3 max,
            Map<Direction, Integer> sideColors, Map<Direction, Integer> lightOverrides,
            Map<Direction, RenderType> targets
    ) {
        output.setSpriteForStaticTargets(QuadBuilder.getWhiteTexture());
        for (Map.Entry<Direction, Integer> entry : sideColors.entrySet()) {
            Direction side = entry.getKey();
            Direction.Axis normal = side.getAxis();
            Direction.Axis orthA = Direction.Axis.values()[(normal.ordinal() + 1) % 3];
            Direction.Axis orthB = Direction.Axis.values()[(normal.ordinal() + 2) % 3];
            final boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            Vec3 posA = positive ? max : min;
            Vec3 posB = positive ? min : max;
            TransformingVertexBuilder out = new TransformingVertexBuilder(
                    output.getBuffer(targets.get(side)), transform
            );
            new QuadBuilder(
                    posA,
                    withValueFrom(posA, positive ? orthA : orthB, posB),
                    withValueFrom(posB, normal, posA),
                    withValueFrom(posA, positive ? orthB : orthA, posB)
            )
                    //TODO other default?
                    .setBlockLightOverride(lightOverrides.getOrDefault(side, 0))
                    .setRGB(entry.getValue())
                    .setNormal(Vec3.atLowerCornerOf(side.getNormal()))
                    .writeTo(out);
        }
    }

    private static Vec3 withValueFrom(Vec3 main, Direction.Axis axis, Vec3 from) {
        return with(main, axis, axis.choose(from.x, from.y, from.z));
    }

    private static Vec3 with(Vec3 in, Direction.Axis axis, double value) {
        return new Vec3(
                axis == Direction.Axis.X ? value : in.x,
                axis == Direction.Axis.Y ? value : in.y,
                axis == Direction.Axis.Z ? value : in.z
        );
    }

}
