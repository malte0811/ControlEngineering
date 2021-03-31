package malte0811.controlengineering.client.render.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

//TODO copied from IE
public class TransformingVertexBuilder extends DelegatingVertexBuilder<TransformingVertexBuilder> {
    private final MatrixStack transform;
    ObjectWithGlobal<Vector2f> uv = new ObjectWithGlobal<>();
    ObjectWithGlobal<Vector3d> pos = new ObjectWithGlobal<>();
    ObjectWithGlobal<Vec2i> overlay = new ObjectWithGlobal<>();
    ObjectWithGlobal<Vec2i> lightmap = new ObjectWithGlobal<>();
    ObjectWithGlobal<Vector3f> normal = new ObjectWithGlobal<>();
    ObjectWithGlobal<Vector4f> color = new ObjectWithGlobal<>();

    public TransformingVertexBuilder(IVertexBuilder base, MatrixStack transform) {
        super(base);
        this.transform = transform;
    }

    public TransformingVertexBuilder(IVertexBuilder base) {
        this(base, new MatrixStack());
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder pos(double x, double y, double z) {
        pos.putData(new Vector3d(x, y, z));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder color(int red, int green, int blue, int alpha) {
        color.putData(new Vector4f(red / 255f, green / 255f, blue / 255f, alpha / 255f));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder tex(float u, float v) {
        uv.putData(new Vector2f(u, v));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder overlay(int u, int v) {
        overlay.putData(new Vec2i(u, v));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder lightmap(int u, int v) {
        lightmap.putData(new Vec2i(u, v));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder normal(float x, float y, float z) {
        normal.putData(new Vector3f(x, y, z));
        return this;
    }

    @Override
    public void endVertex() {
        pos.ifPresent(pos -> delegate.pos(
                transform.getLast().getMatrix(),
                (float) pos.x,
                (float) pos.y,
                (float) pos.z
        ));
        color.ifPresent(c -> delegate.color(c.getX(), c.getY(), c.getZ(), c.getW()));
        uv.ifPresent(uv -> delegate.tex(uv.x, uv.y));
        overlay.ifPresent(overlay -> delegate.overlay(overlay.x, overlay.y));
        lightmap.ifPresent(lightmap -> delegate.lightmap(lightmap.x, lightmap.y));
        normal.ifPresent(normal -> delegate.normal(normal.getX(), normal.getY(), normal.getZ()));
        delegate.endVertex();
    }

    public TransformingVertexBuilder setLight(int light) {
        lightmap.setGlobal(new Vec2i(light & 255, light >> 16));
        return getThis();
    }

    public TransformingVertexBuilder setColor(float r, float g, float b, float a) {
        color.setGlobal(new Vector4f(r, g, b, a));
        return getThis();
    }

    public TransformingVertexBuilder setNormal(float x, float y, float z) {
        Vector3f vec = new Vector3f(x, y, z);
        vec.normalize();
        normal.setGlobal(vec);
        return getThis();
    }

    public TransformingVertexBuilder setOverlay(int packedOverlayIn) {
        overlay.setGlobal(new Vec2i(
                packedOverlayIn & 0xffff,
                packedOverlayIn >> 16
        ));
        return getThis();
    }

    public TransformingVertexBuilder setNormal(Vector3d normal) {
        return setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    @Override
    protected TransformingVertexBuilder getThis() {
        return this;
    }

    public TransformingVertexBuilder setColor(int color) {
        return setColor(
                BitUtils.getBits(color, 16, 8) / 255f,
                BitUtils.getBits(color, 8, 8) / 255f,
                BitUtils.getBits(color, 0, 8) / 255f,
                BitUtils.getBits(color, 24, 8) / 255f
        );
    }

    private static class Vec2i {
        final int x, y;

        private Vec2i(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class ObjectWithGlobal<T> {
        @Nullable
        T obj;
        boolean isGlobal;

        public void putData(T newVal) {
            Preconditions.checkState(obj == null);
            obj = newVal;
        }

        public void setGlobal(@Nullable T obj) {
            this.obj = obj;
            isGlobal = obj != null;
        }

        public T read() {
            T ret = Preconditions.checkNotNull(obj);
            if (!isGlobal)
                obj = null;
            return ret;
        }

        public boolean hasValue() {
            return obj != null;
        }

        public void ifPresent(Consumer<T> out) {
            if (hasValue())
                out.accept(read());
        }
    }
}
