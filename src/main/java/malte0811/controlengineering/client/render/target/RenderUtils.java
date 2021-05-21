package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Map;

public class RenderUtils {
    public static void renderColoredBox(
            MixedModel output,
            MatrixStack transform, Vector3d min, Vector3d max,
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
            Vector3d posA = positive ? max : min;
            Vector3d posB = positive ? min : max;
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
                    .setNormal(Vector3d.copy(side.getDirectionVec()))
                    .writeTo(out);
        }
    }

    private static Vector3d withValueFrom(Vector3d main, Direction.Axis axis, Vector3d from) {
        return with(main, axis, axis.getCoordinate(from.x, from.y, from.z));
    }

    private static Vector3d with(Vector3d in, Direction.Axis axis, double value) {
        return new Vector3d(
                axis == Direction.Axis.X ? value : in.x,
                axis == Direction.Axis.Y ? value : in.y,
                axis == Direction.Axis.Z ? value : in.z
        );
    }

}
