package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

public class RenderUtils {

    public static final Map<Direction, RenderType> ALL_DYNAMIC = Util.make(new EnumMap<>(Direction.class), types -> {
        for (Direction side : DirectionUtils.VALUES) {
            types.put(side, MixedModel.SOLID_DYNAMIC);
        }
    });

    public static final Map<Direction, RenderType> ALL_STATIC = Util.make(new EnumMap<>(Direction.class), types -> {
        for (Direction side : DirectionUtils.VALUES) {
            types.put(side, MixedModel.SOLID_STATIC);
        }
    });

    public static void renderColoredBox(
            MixedModel output,
            PoseStack transform, Vec3 min, Vec3 max,
            Map<Direction, Integer> sideColors, Map<Direction, Integer> lightOverrides,
            Map<Direction, RenderType> targets
    ) {
        renderColoredBox(output, transform, min, max, sideColors, lightOverrides, targets, false);
    }

    public static void renderColoredBox(
            MixedModel output,
            PoseStack transform, Vec3 min, Vec3 max,
            Map<Direction, Integer> sideColors, Map<Direction, Integer> lightOverrides,
            Map<Direction, RenderType> targets, boolean invert
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
                    output.getBuffer(targets.get(side)), transform, DefaultVertexFormat.BLOCK
            );
            Vec3[] vertices = {
                    posA, withValueFrom(posA, positive ? orthA : orthB, posB),
                    withValueFrom(posB, normal, posA), withValueFrom(posA, positive ? orthB : orthA, posB)
            };
            new QuadBuilder(
                    vertices[invert ? 3 : 0], vertices[invert ? 2 : 1],
                    vertices[invert ? 1 : 2], vertices[invert ? 0 : 3]
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

    public static Map<Direction, Integer> makeColorsExcept(int color, Direction... excluded) {
        Map<Direction, Integer> map = new EnumMap<>(Direction.class);
        for (var side : DirectionUtils.VALUES) {
            map.put(side, color);
        }
        for (var excludedSide : excluded) {
            map.remove(excludedSide);
        }
        return map;
    }
}
