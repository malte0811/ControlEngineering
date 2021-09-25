package malte0811.controlengineering.client.render.utils;

import net.minecraft.world.phys.Vec3;

public class ModelRenderUtils {
    private static final int NUM_TUBE_FACES = 4;

    public static void renderTube(
            TransformingVertexBuilder out,
            double diameterLow, double diameterHigh,
            double yMin, double yMax,
            UVCoord minUV, UVCoord maxUV
    ) {
        final double lowerOffset = (diameterHigh - diameterLow) / 2;
        final float deltaU = (maxUV.u - minUV.u) / NUM_TUBE_FACES;
        for (int leftVertex = 0; leftVertex < NUM_TUBE_FACES; ++leftVertex) {
            final Vec3 normal = tubeNormal(leftVertex);
            out.setNormal(normal);

            final int rightVertex = leftVertex + 1;
            final float leftU = deltaU * leftVertex + minUV.u;
            final float rightU = deltaU * rightVertex + minUV.u;
            out.pos(tubeVertex(rightVertex, diameterLow, lowerOffset, yMin))
                    .uv(rightU, minUV.v).endVertex();
            out.pos(tubeVertex(rightVertex, diameterHigh, 0, yMax))
                    .uv(rightU, maxUV.v).endVertex();
            out.pos(tubeVertex(leftVertex, diameterHigh, 0, yMax))
                    .uv(leftU, maxUV.v).endVertex();
            out.pos(tubeVertex(leftVertex, diameterLow, lowerOffset, yMin))
                    .uv(leftU, minUV.v).endVertex();
        }
    }

    private static Vec3 tubeNormal(int vertex) {
        //Not 100% accurate (ignores lower/upper diameter), but good enough
        //TODO actually implement
        return new Vec3(0, 1, 0);
    }

    private static Vec3 tubeVertex(int vertex, double diameter, double offset, double y) {
        switch (vertex % NUM_TUBE_FACES) {
            case 0:
                return new Vec3(offset, y, offset);
            case 1:
                return new Vec3(offset, y, diameter + offset);
            case 2:
                return new Vec3(diameter + offset, y, diameter + offset);
            case 3:
                return new Vec3(diameter + offset, y, offset);
        }
        throw new IllegalStateException();
    }

    public static class UVCoord {
        public final float u;
        public final float v;

        public UVCoord(float u, float v) {
            this.u = u;
            this.v = v;
        }
    }
}
