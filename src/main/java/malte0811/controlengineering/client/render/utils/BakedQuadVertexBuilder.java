package malte0811.controlengineering.client.render.utils;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.util.List;

public class BakedQuadVertexBuilder extends TransformingVertexBuilder {
    private final TextureAtlasSprite sprite;
    private final List<BakedQuad> quads;
    private BakedQuadBuilder builder = null;
    private int nextVertex = 0;
    private boolean interpolateUV = true;

    public BakedQuadVertexBuilder(
            TextureAtlasSprite sprite, PoseStack transform, List<BakedQuad> quads
    ) {
        super(null, transform);
        this.sprite = sprite;
        this.quads = quads;
    }

    public BakedQuadVertexBuilder dontInterpolateUV() {
        interpolateUV = false;
        return this;
    }

    @Override
    public void endVertex() {
        if (builder == null) {
            builder = new BakedQuadBuilder(sprite);
        }
        Vec3 pos = this.pos.read();
        Vector4f transformedPos = new Vector4f((float) pos.x, (float) pos.y, (float) pos.z, 1);
        transformedPos.transform(transform.last().pose());
        transformedPos.perspectiveDivide();
        Vec2 uv = this.uv.read();
        Vector3f normal = this.normal.read();
        normal.transform(transform.last().normal());
        putVertex(
                builder, normal, transformedPos, color.read(),
                interpolateUV ? sprite.getU(uv.x) : uv.x,
                interpolateUV ? sprite.getV(uv.y) : uv.y
        );
        this.lightmap.read();
        this.overlay.read();
        ++nextVertex;
        if (nextVertex == 4) {
            nextVertex = 0;
            builder.setQuadOrientation(Direction.getNearest(normal.x(), normal.y(), normal.z()));
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
                    consumer.put(e, pos.x(), pos.y(), pos.z(), 1f);
                    break;
                case COLOR:
                    consumer.put(e, color.x(), color.y(), color.z(), color.w());
                    break;
                case NORMAL:
                    consumer.put(e, normal.x(), normal.y(), normal.z(), 0);
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
