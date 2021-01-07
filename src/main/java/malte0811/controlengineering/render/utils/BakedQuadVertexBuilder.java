package malte0811.controlengineering.render.utils;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import javax.annotation.Nonnull;

//TODO is this even possible? Quads want a TAS for no reason…
public class BakedQuadVertexBuilder implements IVertexBuilder {
    @Nonnull
    @Override
    public IVertexBuilder pos(double x, double y, double z) {
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder color(int red, int green, int blue, int alpha) {
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder tex(float u, float v) {
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder overlay(int u, int v) {
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder lightmap(int u, int v) {
        return this;
    }

    @Nonnull
    @Override
    public IVertexBuilder normal(float x, float y, float z) {
        return this;
    }

    @Override
    public void endVertex() {

    }
}
