package malte0811.controlengineering.util.math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

public class TransformUtil {
    public static Matrix4f shear(float deltaXPerY, float deltaZPerY) {
        return new Matrix4f(
                new float[]{
                        1, deltaXPerY, 0, 0,
                        0, 1, 0, 0,
                        0, deltaZPerY, 1, 0,
                        0, 0, 0, 1,
                }
        );
    }

    public static Matrix3f shearNormal(float deltaXPerY, float deltaZPerY) {
        // A bit hacky, but there's no direct constructor for Matrix3f
        Matrix3f result = new Matrix3f(shear(-deltaXPerY, -deltaZPerY));
        result.transpose();
        return result;
    }

    public static void shear(PoseStack transform, float deltaXPerY, float deltaZPerY) {
        transform.last().pose().multiply(shear(deltaXPerY, deltaZPerY));
        transform.last().normal().mul(shearNormal(deltaXPerY, deltaZPerY));
    }
}
