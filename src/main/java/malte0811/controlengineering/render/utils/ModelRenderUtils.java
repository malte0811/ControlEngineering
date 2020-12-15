package malte0811.controlengineering.render.utils;

import net.minecraft.util.math.vector.Vector3d;

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
            final Vector3d normal = tubeNormal(leftVertex);
            out.setNormal(normal);

            final int rightVertex = leftVertex + 1;
            final float leftU = deltaU * leftVertex + minUV.u;
            final float rightU = deltaU * rightVertex + minUV.u;
            out.pos(tubeVertex(rightVertex, diameterLow, lowerOffset, yMin))
                    .tex(rightU, minUV.v).endVertex();
            out.pos(tubeVertex(rightVertex, diameterHigh, 0, yMax))
                    .tex(rightU, maxUV.v).endVertex();
            out.pos(tubeVertex(leftVertex, diameterHigh, 0, yMax))
                    .tex(leftU, maxUV.v).endVertex();
            out.pos(tubeVertex(leftVertex, diameterLow, lowerOffset, yMin))
                    .tex(leftU, minUV.v).endVertex();
        }
    }

    private static Vector3d tubeNormal(int vertex) {
        //Not 100% accurate (ignores lower/upper diameter), but good enough
        //TODO actually implement
        return new Vector3d(0, 1, 0);
    }

    private static Vector3d tubeVertex(int vertex, double diameter, double offset, double y) {
        switch (vertex % NUM_TUBE_FACES) {
            case 0:
                return new Vector3d(offset, y, offset);
            case 1:
                return new Vector3d(offset, y, diameter + offset);
            case 2:
                return new Vector3d(diameter + offset, y, diameter + offset);
            case 3:
                return new Vector3d(diameter + offset, y, offset);
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
