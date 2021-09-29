package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DynamicVertexBuilder implements VertexConsumer {
    private List<DynamicVertex.Step> inVertex = new ArrayList<>();
    private final List<DynamicVertex> finishedVertices;

    public DynamicVertexBuilder(List<DynamicVertex> finishedVertices) {
        this.finishedVertices = finishedVertices;
    }

    @Nonnull
    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        inVertex.add((v, $1, $2) -> v.vertex(x, y, z));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        inVertex.add((v, $1, $2) -> v.color(red, green, blue, alpha));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer uv(float u, float v) {
        inVertex.add((out, $1, $2) -> out.uv(u, v));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        inVertex.add((out, $1, overlay) -> out.overlayCoords(overlay));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer uv2(int uSelf, int vSelf) {
        inVertex.add((out, light, $) -> {
            final int uExternal = light & 0xffff;
            final int vExternal = light >> 16 & 0xffff;
            out.uv2(Math.max(uExternal, uSelf << 4), Math.max(vExternal, vSelf << 4));
        });
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer normal(float x, float y, float z) {
        inVertex.add((v, $1, $2) -> v.normal(x, y, z));
        return this;
    }

    @Override
    public void endVertex() {
        finishedVertices.add(new DynamicVertex(inVertex));
        inVertex = new ArrayList<>();
    }

    @Override
    public void defaultColor(int pRed, int pGreen, int pBlue, int pAlpha) {
        inVertex.add((out, $1, $2) -> out.defaultColor(pRed, pGreen, pBlue, pAlpha));
    }

    @Override
    public void unsetDefaultColor() {
        inVertex.add((out, $1, $2) -> out.unsetDefaultColor());
    }
}
