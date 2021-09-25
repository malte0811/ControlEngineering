package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nonnull;
import net.minecraft.world.phys.Vec3;

public abstract class DelegatingVertexBuilder<T extends DelegatingVertexBuilder<T>> implements VertexConsumer {
    protected final VertexConsumer delegate;

    protected DelegatingVertexBuilder(VertexConsumer delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    @Override
    public T vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
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
    public T uv(float u, float v) {
        delegate.uv(u, v);
        return getThis();
    }

    @Nonnull
    @Override
    public T overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return getThis();
    }

    @Nonnull
    @Override
    public T uv2(int u, int v) {
        delegate.uv2(u, v);
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

    public T pos(Vec3 pos) {
        return vertex((float) pos.x, (float) pos.y, (float) pos.z);
    }

    protected abstract T getThis();
}
