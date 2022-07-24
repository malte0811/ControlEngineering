package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import javax.annotation.Nonnull;
import java.util.List;

public class BakedQuadVertexBuilder {
    public static VertexConsumer makeNonInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new QuadBakingVertexConsumer(quads::add);
        baker.setSprite(sprite);
        return new TransformingVertexBuilder(baker, transform, DefaultVertexFormat.BLOCK);
    }

    public static VertexConsumer makeInterpolating(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        final var baker = new QuadBakingVertexConsumer(quads::add);
        baker.setSprite(sprite);
        return new TransformingVertexBuilder(baker, transform, DefaultVertexFormat.BLOCK) {
            @Nonnull
            @Override
            public TransformingVertexBuilder uv(float u, float v) {
                return super.uv(sprite.getU(u), sprite.getV(v));
            }
        };
    }
}
