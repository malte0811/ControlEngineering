package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import java.util.function.Predicate;

public class DynamicRenderTarget extends RenderTarget {
    private final IVertexBuilder builder;

    public DynamicRenderTarget(IVertexBuilder builder, int baseLight, int overlay, Predicate<TargetType> doRender) {
        super(doRender, baseLight, overlay);
        this.builder = builder;
    }

    @Override
    protected void addVertex(
            Vector4f pos, Vector3f normal,
            float red, float green, float blue, float alpha,
            float texU, float texV, int overlayUV, int lightmapUV
    ) {
        builder.pos(pos.getX(), pos.getY(), pos.getZ());
        builder.color(red, green, blue, alpha);
        builder.tex(texU, texV);
        builder.overlay(overlayUV);
        builder.lightmap(lightmapUV);
        builder.normal(normal.getX(), normal.getY(), normal.getZ());
        builder.endVertex();
    }
}
