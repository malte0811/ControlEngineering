package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DynamicVertexBuilder implements IVertexBuilder {
    private List<DynamicVertex.Step> inVertex = new ArrayList<>();
    private final List<DynamicVertex> finishedVertices;

    public DynamicVertexBuilder(List<DynamicVertex> finishedVertices) {
        this.finishedVertices = finishedVertices;
    }

    @Nonnull
    @Override
    public IVertexBuilder pos(double x, double y, double z) {
        inVertex.add((v, $1, $2) -> v.pos(x, y, z));
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder color(int red, int green, int blue, int alpha) {
        inVertex.add((v, $1, $2) -> v.color(red, green, blue, alpha));
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder tex(float u, float v) {
        inVertex.add((out, $1, $2) -> out.tex(u, v));
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder overlay(int u, int v) {
        inVertex.add((out, $1, overlay) -> out.overlay(overlay));
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder lightmap(int uSelf, int vSelf) {
        inVertex.add((out, light, $) -> {
            final int uExternal = light & 0xffff;
            final int vExternal = light >> 16 & 0xffff;
            out.lightmap(Math.max(uExternal, uSelf << 4), Math.max(vExternal, vSelf << 4));
        });
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        inVertex.add((v, $1, $2) -> v.normal(x, y, z));
        return this;
    }

    @Override
    public void endVertex() {
        finishedVertices.add(new DynamicVertex(inVertex));
        inVertex = new ArrayList<>();
    }
}
