package malte0811.controlengineering.client.render.utils;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ModelRenderUtils {
    private static final int NUM_TUBE_FACES = 4;

    public static void renderTube(
            TransformingVertexBuilder out,
            double diameterLow, double diameterHigh,
            double yMin, double yMax,
            UVCoord minUV, UVCoord maxUV
    ) {
        final double lowerOffset = (diameterHigh - diameterLow) / 2;
        final float deltaV = (maxUV.v - minUV.v) / NUM_TUBE_FACES;
        for (int leftVertex = 0; leftVertex < NUM_TUBE_FACES; ++leftVertex) {
            out.setNormal(tubeNormal(leftVertex));

            final int rightVertex = leftVertex + 1;
            final float leftV = deltaV * leftVertex + minUV.v;
            final float rightV = deltaV * rightVertex + minUV.v;
            out.addVertex(tubeVertex(rightVertex, diameterLow, lowerOffset, yMin))
                    .setUv(minUV.u, rightV);
            out.addVertex(tubeVertex(rightVertex, diameterHigh, 0, yMax))
                    .setUv(maxUV.u, rightV);
            out.addVertex(tubeVertex(leftVertex, diameterHigh, 0, yMax))
                    .setUv(maxUV.u, leftV);
            out.addVertex(tubeVertex(leftVertex, diameterLow, lowerOffset, yMin))
                    .setUv(minUV.u, leftV);
        }
    }

    private static Vector3f tubeNormal(int vertex) {
        //Not 100% accurate (ignores lower/upper diameter), but good enough
        //TODO actually implement
        return new Vector3f(0, 1, 0);
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

    public record UVCoord(float u, float v) { }
}
