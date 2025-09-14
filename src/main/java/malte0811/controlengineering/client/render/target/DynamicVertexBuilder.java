package malte0811.controlengineering.client.render.target;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DynamicVertexBuilder implements VertexConsumer {
    private List<DynamicVertex.Step> inVertex = new ArrayList<>();
    private final List<DynamicVertex> finishedVertices = new ArrayList<>();

    @Nonnull
    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
            finishVertex();
        inVertex.add((v, $1, $2) -> v.addVertex(x, y, z));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        inVertex.add((v, $1, $2) -> v.setColor(red, green, blue, alpha));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer setUv(float u, float v) {
        inVertex.add((out, $1, $2) -> out.setUv(u, v));
        return this;
    }

    @Override
    @Nonnull
    public VertexConsumer setUv1(int u, int v) {
        inVertex.add((out, $1, $2) -> out.setUv1(u, v));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer setOverlay(int ignoredOverlay) {
        inVertex.add((out, $1, overlay) -> out.setOverlay(overlay));
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer setUv2(int uSelf, int vSelf) {
        inVertex.add((out, light, $) -> {
            final int uExternal = light & 0xffff;
            final int vExternal = light >> 16 & 0xffff;
            out.setUv2(Math.max(uExternal, uSelf << 4), Math.max(vExternal, vSelf << 4));
        });
        return this;
    }

    @Nonnull
    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        inVertex.add((v, $1, $2) -> v.setNormal(x, y, z));
        return this;
    }

    public void finishVertex() {
        if (!inVertex.isEmpty()) {
            finishedVertices.add(new DynamicVertex(inVertex));
            inVertex = new ArrayList<>();
        }
    }

    public List<DynamicVertex> getFinishedVertices() {
        finishVertex();
        return finishedVertices;
    }
}
