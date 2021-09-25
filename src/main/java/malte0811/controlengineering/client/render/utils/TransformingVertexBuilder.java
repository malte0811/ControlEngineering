package malte0811.controlengineering.client.render.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

//TODO copied from IE
public class TransformingVertexBuilder extends DelegatingVertexBuilder<TransformingVertexBuilder> {
    protected final PoseStack transform;
    protected final ObjectWithGlobal<Vec2> uv = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec3> pos = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec2i> overlay = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec2i> lightmap = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vector3f> normal = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vector4f> color = new ObjectWithGlobal<>();

    public TransformingVertexBuilder(VertexConsumer base, PoseStack transform) {
        super(base);
        this.transform = transform;
    }

    public TransformingVertexBuilder(VertexConsumer base) {
        this(base, new PoseStack());
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder vertex(double x, double y, double z) {
        pos.putData(new Vec3(x, y, z));
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
    public TransformingVertexBuilder uv(float u, float v) {
        uv.putData(new Vec2(u, v));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder overlayCoords(int u, int v) {
        overlay.putData(new Vec2i(u, v));
        return this;
    }

    @Nonnull
    @Override
    public TransformingVertexBuilder uv2(int u, int v) {
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
        pos.ifPresent(pos -> delegate.vertex(
                transform.last().pose(),
                (float) pos.x,
                (float) pos.y,
                (float) pos.z
        ));
        color.ifPresent(c -> delegate.color(c.x(), c.y(), c.z(), c.w()));
        uv.ifPresent(uv -> delegate.uv(uv.x, uv.y));
        overlay.ifPresent(overlay -> delegate.overlayCoords(overlay.x, overlay.y));
        lightmap.ifPresent(lightmap -> delegate.uv2(lightmap.x, lightmap.y));
        normal.ifPresent(normal -> delegate.normal(normal.x(), normal.y(), normal.z()));
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

    public TransformingVertexBuilder setNormal(Vec3 normal) {
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

    protected static class ObjectWithGlobal<T> {
        @Nullable
        private T obj;
        private boolean isGlobal;

        public void putData(T newVal) {
            if (!isGlobal) {
                obj = newVal;
            }
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
