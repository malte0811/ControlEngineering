package malte0811.controlengineering.client.render.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

//TODO copied from IE
public class TransformingVertexBuilder extends DelegatingVertexBuilder<TransformingVertexBuilder> {
    protected final PoseStack transform;
    protected final List<ObjectWithGlobal<?>> allObjects = new ArrayList<>();
    protected final ObjectWithGlobal<Vec2> uv = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec3> pos = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec2i> overlay = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vec2i> lightmap = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vector3f> normal = new ObjectWithGlobal<>();
    protected final ObjectWithGlobal<Vector4f> color = new ObjectWithGlobal<>();
    protected final VertexFormat format;

    public TransformingVertexBuilder(VertexConsumer base, PoseStack transform, VertexFormat format) {
        super(base);
        this.transform = transform;
        this.format = format;
    }

    public TransformingVertexBuilder(VertexConsumer base, VertexFormat format) {
        this(base, new PoseStack(), format);
    }

    public TransformingVertexBuilder(MultiBufferSource buffer, RenderType type, PoseStack transform) {
        this(buffer.getBuffer(type), transform, type.format());
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
        for (VertexFormatElement element : format.getElements()) {
            if (element == ELEMENT_POSITION)
                pos.ifPresent(pos -> delegate.vertex(
                        transform.last().pose(),
                        (float) pos.x,
                        (float) pos.y,
                        (float) pos.z
                ));
            else if (element == ELEMENT_COLOR)
                color.ifPresent(c -> delegate.color(c.x(), c.y(), c.z(), c.w()));
            else if (element == ELEMENT_UV0)
                uv.ifPresent(uv -> delegate.uv(uv.x, uv.y));
            else if (element == ELEMENT_UV1)
                overlay.ifPresent(overlay -> delegate.overlayCoords(overlay.x, overlay.y));
            else if (element == ELEMENT_UV2)
                lightmap.ifPresent(lightmap -> delegate.uv2(lightmap.x, lightmap.y));
            else if (element == ELEMENT_NORMAL)
                normal.ifPresent(
                        normal -> delegate.normal(transform.last().normal(), normal.x(), normal.y(), normal.z())
                );
        }
        delegate.endVertex();
        allObjects.forEach(ObjectWithGlobal::clear);
    }

    public void defaultColor(float r, float g, float b, float a) {
        color.setGlobal(new Vector4f(r, g, b, a));
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        defaultColor(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    @Override
    public void unsetDefaultColor() {
        color.setGlobal(null);
    }

    public TransformingVertexBuilder setLight(int light) {
        lightmap.setGlobal(new Vec2i(light & 255, light >> 16));
        return getThis();
    }

    public TransformingVertexBuilder setNormal(float x, float y, float z) {
        Vector3f vec = new Vector3f(x, y, z);
        vec.normalize();
        normal.setGlobal(vec);
        return getThis();
    }

    public TransformingVertexBuilder setNormal(Vec3 normal) {
        return setNormal((float) normal.x, (float) normal.y, (float) normal.z);
    }

    public TransformingVertexBuilder setOverlay(int packedOverlayIn) {
        overlay.setGlobal(new Vec2i(packedOverlayIn & 0xffff, packedOverlayIn >> 16));
        return getThis();
    }

    @Override
    protected TransformingVertexBuilder getThis() {
        return this;
    }

    public TransformingVertexBuilder setColor(int color) {
        defaultColor(
                BitUtils.getBits(color, 16, 8) / 255f,
                BitUtils.getBits(color, 8, 8) / 255f,
                BitUtils.getBits(color, 0, 8) / 255f,
                BitUtils.getBits(color, 24, 8) / 255f
        );
        return this;
    }

    @Nonnull
    @Override
    public VertexFormat getVertexFormat() {
        return format;
    }

    private record Vec2i(int x, int y) {
    }

    protected class ObjectWithGlobal<T> {
        @Nullable
        private T obj;
        private boolean isGlobal;

        public ObjectWithGlobal() {
            allObjects.add(this);
        }

        public void putData(T newVal) {
            if (obj == null)
                obj = newVal;
        }

        public void setGlobal(@Nullable T obj) {
            this.obj = obj;
            isGlobal = obj != null;
        }

        public T read() {
            T ret = Preconditions.checkNotNull(obj);
            clear();
            return ret;
        }

        public boolean hasValue() {
            return obj != null;
        }

        public void clear() {
            if (!isGlobal)
                obj = null;
        }

        public void ifPresent(Consumer<T> out) {
            if (hasValue())
                out.accept(read());
        }
    }
}
