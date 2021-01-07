package malte0811.controlengineering.controlpanels.renders.target;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class StaticRenderTarget extends RenderTarget {
    // Existing quads
    private final List<BakedQuad> quads = new ArrayList<>();
    // Current quad
    private BakedQuadBuilder builder;
    private int nextVertex = 0;

    public StaticRenderTarget(Predicate<TargetType> doRender) {
        super(doRender, 0, 0);
    }

    @Override
    protected void addVertex(
            Vector3f pos, Vector3f normal,
            float red, float green, float blue, float alpha,
            float texU, float texV, int overlayUV, int lightmapUV
    ) {
        if (builder == null) {
            builder = new BakedQuadBuilder(getTexture());
            builder.setQuadTint(0);
            builder.setQuadOrientation(Direction.getFacingFromVector(normal.getX(), normal.getY(), normal.getZ()));
            builder.setApplyDiffuseLighting(false);
        }
        putVertex(builder, normal, pos, new Vector4f(red, green, blue, alpha), texU, texV);
        ++nextVertex;
        if (nextVertex >= 4) {
            nextVertex = 0;
            quads.add(builder.build());
            builder = null;
        }
    }

    public List<BakedQuad> getQuads() {
        return quads;
    }

    private static void putVertex(
            IVertexConsumer consumer, Vector3f normal, Vector3f pos, Vector4f color,
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
