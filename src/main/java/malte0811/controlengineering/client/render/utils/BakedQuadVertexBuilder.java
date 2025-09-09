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
    public static VertexConsumer makeNonInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new ExtendedQuadVertexConsumer(quads::add);
        baker.setSprite(sprite);
        baker.setHasAmbientOcclusion(true);
        baker.setShade(true);
        return new TransformingVertexBuilder(baker, transform);
    }

    public static VertexConsumer makeInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new ExtendedQuadVertexConsumer(quads::add);
        baker.setSprite(sprite);
        baker.setHasAmbientOcclusion(true);
        baker.setShade(true);
        return new TransformingVertexBuilder(baker, transform) {
            @Nonnull
            @Override
            public VertexConsumer setUv(float u, float v) {
                return super.setUv(sprite.getU(u), sprite.getV(v));
            }
        };
    }

    private static class ExtendedQuadVertexConsumer extends QuadBakingVertexConsumer {
        private final Consumer<BakedQuad> onQuad;
        private int vertices = 0;

        private ExtendedQuadVertexConsumer(Consumer<BakedQuad> onQuad) { this.onQuad = onQuad; }

        @Override
        public VertexConsumer addVertex(Vector3f p_350685_) {
            if (vertices == 4) {
                onQuad.accept(bakeQuad());
                vertices = 0;
            }
            ++vertices;
            return super.addVertex(p_350685_);
        }
    }
}
