package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import java.util.List;

public class BakedQuadVertexBuilder extends TransformingVertexBuilder {
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    private BakedQuadBuilder builder = null;
    private int nextVertex = 0;

    public BakedQuadVertexBuilder(
            TextureAtlasSprite sprite, MatrixStack transform, List<BakedQuad> quads
    ) {
        super(null, transform);
        this.sprite = sprite;
        this.quads = quads;
    }

    @Override
    public void endVertex() {
        if (builder == null) {
            builder = new BakedQuadBuilder(sprite);
        }
        Vector3d pos = this.pos.read();
        Vector2f uv = this.uv.read();
        Vector3f normal = this.normal.read();
        putVertex(
                builder,
                normal,
                new Vector4f((float) pos.x, (float) pos.y, (float) pos.z, 0),
                color.read(),
                sprite.getInterpolatedU(uv.x), sprite.getInterpolatedV(uv.y)
        );
        this.lightmap.read();
        this.overlay.read();
        ++nextVertex;
        if (nextVertex == 4) {
            nextVertex = 0;
            builder.setQuadOrientation(Direction.getFacingFromVector(normal.getX(), normal.getY(), normal.getZ()));
            quads.add(builder.build());
            builder = null;
        }
    }

    private static void putVertex(
            IVertexConsumer consumer, Vector3f normal, Vector4f pos, Vector4f color,
            float u, float v
    ) {
        VertexFormat format = consumer.getVertexFormat();
        for (int e = 0; e < format.getElements().size(); e++) {
            VertexFormatElement element = format.getElements().get(e);
            outer:
            switch (element.getUsage()) {
                case POSITION:
                    consumer.put(e, pos.getX(), pos.getY(), pos.getZ(), 1f);
                    break;
                case COLOR:
                    consumer.put(e, color.getX(), color.getY(), color.getZ(), color.getW());
                    break;
                case NORMAL:
                    consumer.put(e, normal.getX(), normal.getY(), normal.getZ(), 0);
                    break;
                case UV:
                    switch (element.getIndex()) {
                        case 0:
                            consumer.put(e, u, v, 0f, 1f);
                            break outer;
                        case 2:
                            consumer.put(e, 0, 0, 0, 1);
                            break outer;
                    }
                    // else fallthrough to default
                default:
                    consumer.put(e);
                    break;
            }
        }
    }
}
