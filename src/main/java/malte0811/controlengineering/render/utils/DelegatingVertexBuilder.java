package malte0811.controlengineering.render.utils;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Vector3d;

import javax.annotation.Nonnull;

public abstract class DelegatingVertexBuilder<T extends DelegatingVertexBuilder<T>> implements IVertexBuilder {
    protected final IVertexBuilder delegate;

    protected DelegatingVertexBuilder(IVertexBuilder delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public T pos(double x, double y, double z) {
        delegate.pos(x, y, z);
        return getThis();
    }

    @Nonnull
    @Override
    public T color(int red, int green, int blue, int alpha) {
        delegate.color(red, green, blue, alpha);
        return getThis();
    }

    @Nonnull
    @Override
    public T tex(float u, float v) {
        delegate.tex(u, v);
        return getThis();
    }

    @Nonnull
    @Override
    public T overlay(int u, int v) {
        delegate.overlay(u, v);
        return getThis();
    }

    @Nonnull
    @Override
    public T lightmap(int u, int v) {
        delegate.lightmap(u, v);
        return getThis();
    }

    @Nonnull
    @Override
    public T normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return getThis();
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    public T pos(Vector3d pos) {
        return pos((float) pos.x, (float) pos.y, (float) pos.z);
    }

    protected abstract T getThis();
}
