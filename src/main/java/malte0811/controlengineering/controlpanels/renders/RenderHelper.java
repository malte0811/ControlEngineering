package malte0811.controlengineering.controlpanels.renders;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.ModelLoader;

import java.util.EnumMap;
import java.util.Map;

public class RenderHelper {
    private final IVertexBuilder builder;

    public RenderHelper(IVertexBuilder builder) {
        this.builder = builder;
    }

    public void renderColoredQuad(
            MatrixStack transform,
            Vector3d vec1,
            Vector3d vec2,
            Vector3d vec3,
            Vector3d vec4,
            Vector3d normal,
            int color
    ) {
        TextureAtlasSprite texture = ModelLoader.White.instance();
        for (Pair<Vector3d, Vec2d> vec : ImmutableList.of(
                Pair.of(vec1, new Vec2d(texture.getMinU(), texture.getMinV())),
                Pair.of(vec2, new Vec2d(texture.getMinU(), texture.getMaxV())),
                Pair.of(vec3, new Vec2d(texture.getMaxU(), texture.getMaxV())),
                Pair.of(vec4, new Vec2d(texture.getMaxU(), texture.getMinV()))
    )) {
            addVertex(
                    transform.getLast(),
                    vec.getFirst(),
                    extract8BitFloat(color, 16),
                    extract8BitFloat(color, 8),
                    extract8BitFloat(color, 0),
                    1,
                    (float) vec.getSecond().x,
                    (float) vec.getSecond().y,
                    OverlayTexture.NO_OVERLAY,
                    //TODO
                    LightTexture.packLight(15, 15),
                    normal
            );
        }
    }

    public void renderColoredBox(
            MatrixStack transform,
            Vector3d min,
            Vector3d max,
            EnumMap<Direction, Integer> sideColors
    ) {
        for (Map.Entry<Direction, Integer> side : sideColors.entrySet()) {
            Direction.Axis normal = side.getKey().getAxis();
            //TODO may need to be swapped?
            Direction.Axis orthA = Direction.Axis.values()[(normal.ordinal() + 1) % 3];
            Direction.Axis orthB = Direction.Axis.values()[(normal.ordinal() + 2) % 3];
            if (side.getKey().getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                renderColoredQuad(
                        transform,
                        max,
                        withValueFrom(max, orthA, min),
                        withValueFrom(min, normal, max),
                        withValueFrom(max, orthB, min),
                        Vector3d.copy(side.getKey().getDirectionVec()),
                        side.getValue()
                );
            } else {
                renderColoredQuad(
                        transform,
                        min,
                        withValueFrom(min, orthB, max),
                        withValueFrom(max, normal, min),
                        withValueFrom(min, orthA, max),
                        Vector3d.copy(side.getKey().getDirectionVec()),
                        side.getValue()
                );
            }
        }
    }

    private void addVertex(
            MatrixStack.Entry transform,
            Vector3d pos,
            float red,
            float green,
            float blue,
            float alpha,
            float texU,
            float texV,
            int overlayUV,
            int lightmapUV,
            Vector3d normal
    ) {
        builder.pos(transform.getMatrix(), (float) pos.x, (float) pos.y, (float) pos.z);
        builder.color(red, green, blue, alpha);
        builder.tex(texU, texV);
        builder.overlay(overlayUV);
        builder.lightmap(lightmapUV);
        builder.normal(transform.getNormal(), (float) normal.x, (float) normal.y, (float) normal.z);
        builder.endVertex();
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

    private static float extract8BitFloat(int value, int offset) {
        return ((value >> offset) & 255) / 255f;
    }
}
