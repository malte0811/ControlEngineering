package malte0811.controlengineering.controlpanels.renders.target;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
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

    void addVertex(
            Vector4f pos, Vector3f normal,
            float red, float green, float blue, float alpha,
            float texU, float texV, OptionalInt lightOverride
    ) {
        addVertex(pos, normal, red, green, blue, alpha, texU, texV, overlay, lightOverride.orElse(baseLight));
    }

    protected abstract void addVertex(
            Vector4f pos, Vector3f normal,
            float red, float green, float blue, float alpha,
            float texU, float texV, int overlayUV, int lightmapUV
    );

    void setTexture(TextureAtlasSprite newTexture) {
        texture = newTexture;
    }

    public TextureAtlasSprite getTexture() {
        return texture;
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
            final boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            Vector3d posA = positive ? max : min;
            Vector3d posB = positive ? min : max;
            QuadBuilder builder = new QuadBuilder(
                    posA,
                    withValueFrom(posA, positive ? orthA : orthB, posB),
                    withValueFrom(posB, normal, posA),
                    withValueFrom(posA, positive ? orthB : orthA, posB)
            )
                    .setNormal(Vector3d.copy(side.getDirectionVec()))
                    .setRGB(entry.getValue());
            if (lightOverrides.containsKey(side)) {
                builder.setLightOverride(lightOverrides.get(side));
            }
            builder.writeTo(transform, this, targets.get(side));
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

    public boolean isEnabled(TargetType type) {
        return doRender.test(type);
    }
}
