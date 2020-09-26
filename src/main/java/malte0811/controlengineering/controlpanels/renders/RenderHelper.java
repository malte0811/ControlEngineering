package malte0811.controlengineering.controlpanels.renders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.data.AnimationFrame;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class RenderHelper {
    private final IVertexBuilder builder;
    private final int baseLight;
    private final int overlay;

    public RenderHelper(IVertexBuilder builder, int baseLight, int overlay) {
        this.builder = builder;
        this.baseLight = baseLight;
        this.overlay = overlay;
    }

    public void renderColoredQuad(
            MatrixStack transform,
            Vector3d vec1,
            Vector3d vec2,
            Vector3d vec3,
            Vector3d vec4,
            Vector3d normal,
            int color,
            OptionalInt lightOverride
    ) {
        ResourceLocation loc = new ResourceLocation("block/white_wool");
        TextureAtlasSprite texture = Minecraft.getInstance()
                .getModelManager()
                .getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                .getSprite(
                loc);
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
                    overlay,
                    lightOverride.orElse(baseLight),
                    normal
            );
        }
    }

    public void renderColoredBox(
            MatrixStack transform,
            Vector3d min,
            Vector3d max,
            Map<Direction, Integer> sideColors,
            Map<Direction, Integer> lightOverrides
    ) {
        for (Map.Entry<Direction, Integer> entry : sideColors.entrySet()) {
            Direction side = entry.getKey();
            Direction.Axis normal = side.getAxis();
            Direction.Axis orthA = Direction.Axis.values()[(normal.ordinal() + 1) % 3];
            Direction.Axis orthB = Direction.Axis.values()[(normal.ordinal() + 2) % 3];
            OptionalInt light = lightOverrides.containsKey(side) ?
                    OptionalInt.of(lightOverrides.get(side)) :
                    OptionalInt.empty();
            if (side.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                renderColoredQuad(
                        transform,
                        max,
                        withValueFrom(max, orthA, min),
                        withValueFrom(min, normal, max),
                        withValueFrom(max, orthB, min),
                        Vector3d.copy(side.getDirectionVec()),
                        entry.getValue(),
                        light
                );
            } else {
                renderColoredQuad(
                        transform,
                        min,
                        withValueFrom(min, orthB, max),
                        withValueFrom(max, normal, min),
                        withValueFrom(min, orthA, max),
                        Vector3d.copy(side.getDirectionVec()),
                        entry.getValue(),
                        light
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
