package malte0811.controlengineering.controlpanels.renders.target;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Predicate;

public abstract class RenderTarget {
    private final Predicate<TargetType> doRender;
    private final int baseLight;
    private final int overlay;
    private TextureAtlasSprite texture;

    protected RenderTarget(Predicate<TargetType> doRender, int baseLight, int overlay) {
        this.doRender = doRender;
        this.baseLight = baseLight;
        this.overlay = overlay;
    }

    protected abstract void addVertex(
            Vector3f pos, Vector3f normal,
            float red, float green, float blue, float alpha,
            float texU, float texV, int overlayUV, int lightmapUV
    );

    private void setTexture(TextureAtlasSprite newTexture) {
        texture = newTexture;
    }

    public TextureAtlasSprite getTexture() {
        return texture;
    }

    public void renderColoredQuad(
            MatrixStack transform,
            Vector3d vec1, Vector3d vec2, Vector3d vec3, Vector3d vec4,
            Vector3d normal, int color, OptionalInt lightOverride, TargetType target
    ) {
        renderTexturedQuad(transform, getWhiteTexture(), vec1, vec2, vec3, vec4, normal, color, lightOverride, target);
    }

    public void renderTexturedQuad(
            MatrixStack transform, TextureAtlasSprite texture,
            Vector3d vec1, Vector3d vec2, Vector3d vec3, Vector3d vec4,
            Vector3d normal, int color, OptionalInt lightOverride, TargetType target
    ) {
        if (!doRender.test(target)) {
            return;
        }
        setTexture(texture);
        for (Pair<Vector3d, Vec2d> vec : ImmutableList.of(
                Pair.of(vec1, new Vec2d(0, 0)),
                Pair.of(vec2, new Vec2d(0, 16)),
                Pair.of(vec3, new Vec2d(16, 16)),
                Pair.of(vec4, new Vec2d(16, 0))
        )) {
            addVertex(
                    transform.getLast(), vec.getFirst(),
                    normal, extract8BitFloat(color, 16), extract8BitFloat(color, 8), extract8BitFloat(color, 0), 1,
                    (float) vec.getSecond().x, (float) vec.getSecond().y, overlay, lightOverride.orElse(baseLight)
            );
        }
    }

    private void addVertex(
            MatrixStack.Entry last, Vector3d pos, Vector3d normal,
            float r, float g, float b, float alpha,
            float u, float v, int overlay, int light
    ) {
        Vector4f posF = new Vector4f((float) pos.x, (float) pos.y, (float) pos.z, 1);
        Vector3f normalF = new Vector3f(normal);
        posF.transform(last.getMatrix());
        normalF.transform(last.getNormal());
        posF.perspectiveDivide();
        addVertex(
                new Vector3f(posF.getX(), posF.getY(), posF.getZ()), normalF, r, g, b, alpha,
                getTexture().getInterpolatedU(u), getTexture().getInterpolatedV(v), overlay, light
        );
    }

    public void renderColoredBox(
            MatrixStack transform, Vector3d min, Vector3d max,
            Map<Direction, Integer> sideColors, Map<Direction, Integer> lightOverrides,
            Map<Direction, TargetType> targets
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
                        transform, max,
                        withValueFrom(max, orthA, min), withValueFrom(min, normal, max), withValueFrom(max, orthB, min),
                        Vector3d.copy(side.getDirectionVec()), entry.getValue(), light, targets.get(side)
                );
            } else {
                renderColoredQuad(
                        transform, min,
                        withValueFrom(min, orthB, max), withValueFrom(max, normal, min), withValueFrom(min, orthA, max),
                        Vector3d.copy(side.getDirectionVec()), entry.getValue(), light, targets.get(side)
                );
            }
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

    private static float extract8BitFloat(int value, int offset) {
        return ((value >> offset) & 255) / 255f;
    }

    public static TextureAtlasSprite getWhiteTexture() {
        //TODO Forge PR to fix "real" white texture
        ResourceLocation loc = new ResourceLocation("block/white_wool");
        return Minecraft.getInstance()
                .getModelManager()
                .getAtlasTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)
                .getSprite(loc);
    }
}
