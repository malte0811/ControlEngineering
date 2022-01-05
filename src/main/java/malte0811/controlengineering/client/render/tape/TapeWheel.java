package malte0811.controlengineering.client.render.tape;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.BiPredicate;

public class TapeWheel {
    private static final int NUM_CORNERS = 6;
    private static final double TAPE_WIDTH = 1;
    private static final List<Vec2d> CORNERS_NORMALIZED;
    private static final ResettableLazy<TextureAtlasSprite> TEXTURE = new ResettableLazy<>(QuadBuilder::getWhiteTexture);
    private static final List<Pair<Integer, Double>> offsetAndHeight = ImmutableList.of(
            Pair.of(0, 0.),
            Pair.of(1, 0.),
            Pair.of(1, TAPE_WIDTH),
            Pair.of(0, TAPE_WIDTH)
    );

    static {
        IEApi.renderCacheClearers.add(TEXTURE::reset);
        ImmutableList.Builder<Vec2d> corners = ImmutableList.builder();
        for (int i = 0; i < NUM_CORNERS; ++i) {
            final double angle = -i * 2 * Math.PI / NUM_CORNERS;
            corners.add(new Vec2d(Math.cos(angle), Math.sin(angle)));
        }
        CORNERS_NORMALIZED = corners.build();
        Preconditions.checkState(NUM_CORNERS % 2 == 0);
    }

    private final Vec2d tapeTarget;
    private final double maxRenderRadius;
    private final BiPredicate<Double, Double> leftBetter;
    private double radius;
    private double rotationRadians;

    public TapeWheel(Vec2d tapeTarget, double maxRenderRadius, boolean isRight) {
        this.tapeTarget = tapeTarget;
        this.maxRenderRadius = maxRenderRadius;
        if (isRight) {
            leftBetter = (a, b) -> a > b;
        } else {
            leftBetter = (a, b) -> a < b;
        }
    }

    public void render(VertexConsumer output, PoseStack stack, int light, int overlay) {
        SpriteCoordinateExpander spriteBuilder = new SpriteCoordinateExpander(output, TEXTURE.get());
        TransformingVertexBuilder finalBuilder = new TransformingVertexBuilder(
                spriteBuilder, stack, DefaultVertexFormat.BLOCK
        );
        finalBuilder.setColor(0xffa8f9);
        finalBuilder.setNormal(0, 1, 0);
        finalBuilder.setOverlay(overlay);
        finalBuilder.setLight(light);
        renderTapeRoll(finalBuilder, stack);
        renderTapeToTarget(finalBuilder);
    }

    private void renderTapeToTarget(VertexConsumer output) {
        //TODO cache and use convexity (or rather check convexity and then use it...)
        int bestCorner = 0;
        double bestValue = getSlopeFrom(bestCorner);
        for (int newCorner = 1; newCorner < NUM_CORNERS; ++newCorner) {
            double newValue = getSlopeFrom(newCorner);
            if (leftBetter.test(bestValue, newValue)) {
                bestValue = newValue;
                bestCorner = newCorner;
            }
        }
        //TODO normal vector?
        Vec2d[] positions = {cornerRotated(bestCorner), tapeTarget};
        Vec2d cornerUV = cornerRelative(bestCorner);
        //TODO deduplicate
        for (Pair<Integer, Double> pos : offsetAndHeight) {
            Vec2d vec = positions[pos.getFirst()];
            output.vertex(vec.x(), pos.getSecond(), vec.y())
                    .uv(toUV(cornerUV.x()), toUV(cornerUV.y()))
                    .endVertex();
        }
        for (int i = offsetAndHeight.size() - 1; i >= 0; i--) {
            Pair<Integer, Double> pos = offsetAndHeight.get(i);
            Vec2d vec = positions[pos.getFirst()];
            output.vertex(vec.x(), pos.getSecond(), vec.y())
                    .uv(toUV(cornerUV.x()), toUV(cornerUV.y()))
                    .endVertex();
        }
    }

    private double getSlopeFrom(int cornerId) {
        Vec2d corner = cornerRotated(cornerId);
        double deltaX = tapeTarget.x() - corner.x();
        double deltaY = tapeTarget.y() - corner.y();
        return deltaY / deltaX;
    }

    private void renderTapeRoll(VertexConsumer output, PoseStack stack) {
        stack.pushPose();
        stack.mulPose(new Quaternion(0, (float) rotationRadians, 0, false));
        // render top
        for (int i = 1; i + 2 < NUM_CORNERS; i += 2) {
            for (int vertex : new int[]{0, i, i + 1, i + 2}) {
                Vec2d posNormalized = cornerRelative(vertex);
                output.vertex(posNormalized.x(), TAPE_WIDTH, posNormalized.y())
                        .uv(toUV(posNormalized.x()), toUV(posNormalized.y()))
                        .endVertex();
            }
        }
        // render sides
        List<Pair<Integer, Double>> offsetAndHeight = ImmutableList.of(
                Pair.of(0, 0.),
                Pair.of(1, 0.),
                Pair.of(1, TAPE_WIDTH),
                Pair.of(0, TAPE_WIDTH)
        );
        for (int i = 0; i < NUM_CORNERS; ++i) {
            for (Pair<Integer, Double> quadVertex : offsetAndHeight) {
                Vec2d posNormalized = cornerRelative((quadVertex.getFirst() + i) % NUM_CORNERS);
                output.vertex(posNormalized.x(), quadVertex.getSecond(), posNormalized.y())
                        .uv(toUV(posNormalized.x()), toUV(posNormalized.y()))
                        .endVertex();
            }
        }
        stack.popPose();
    }

    private Vec2d cornerRotated(int vertex) {
        final double cos = Math.cos(rotationRadians);
        final double sin = Math.sin(rotationRadians);
        final Vec2d baseVertex = cornerRelative(vertex);
        return new Vec2d(
                cos * baseVertex.x() + sin * baseVertex.y(),
                -sin * baseVertex.x() + cos * baseVertex.y()
        );
    }

    private Vec2d cornerRelative(int vertex) {
        return CORNERS_NORMALIZED.get(vertex).scale(radius * maxRenderRadius);
    }

    private float toUV(double pmMaxRadiusRelative) {
        return Mth.clamp(
                (float) (0.5 * (1 + pmMaxRadiusRelative / maxRenderRadius)),
                1e-3f,
                1 - 1e-3f
        );
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setRotationRadians(double rotationRadians) {
        this.rotationRadians = rotationRadians;
    }
}
