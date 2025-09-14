package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class BakedQuadVertexBuilder {
    public static WrappedConsumer makeMaybeInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads, boolean interpolating
    ) {
        if (interpolating) {
            return makeInterpolating(sprite, transform, quads);
        } else {
            return makeNonInterpolating(sprite, transform, quads);
        }
    }

    public static WrappedConsumer makeNonInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new ExtendedQuadVertexConsumer(quads::add);
        baker.setSprite(sprite);
        baker.setHasAmbientOcclusion(true);
        baker.setShade(true);
        return new WrappedConsumer(new TransformingVertexBuilder(baker, transform), baker);
    }

    public static WrappedConsumer makeInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new ExtendedQuadVertexConsumer(quads::add);
        baker.setSprite(sprite);
        baker.setHasAmbientOcclusion(true);
        baker.setShade(true);
        return new WrappedConsumer(new TransformingVertexBuilder(baker, transform) {
            @Nonnull
            @Override
            public VertexConsumer setUv(float u, float v) {
                return super.setUv(sprite.getU(u), sprite.getV(v));
            }
        }, baker);
    }

    public static class WrappedConsumer implements AutoCloseable {
        private final VertexConsumer visibleConsumer;
        private final ExtendedQuadVertexConsumer quadBuilder;

        private WrappedConsumer(VertexConsumer visibleConsumer, ExtendedQuadVertexConsumer quadBuilder) {
            this.visibleConsumer = visibleConsumer;
            this.quadBuilder = quadBuilder;
        }

        public VertexConsumer consumer() {
            return visibleConsumer;
        }

        @Override
        public void close() {
            quadBuilder.finishVertex();
        }
    }

    private static class ExtendedQuadVertexConsumer extends QuadBakingVertexConsumer {
        private final Consumer<BakedQuad> onQuad;
        private int vertices = 0;

        private ExtendedQuadVertexConsumer(Consumer<BakedQuad> onQuad) { this.onQuad = onQuad; }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            finishVertex();
            return super.addVertex(x, y, z);
        }

        private void finishVertex() {
            if (vertices == 4) {
                onQuad.accept(bakeQuad());
                vertices = 0;
            }
            ++vertices;
        }
    }
}
