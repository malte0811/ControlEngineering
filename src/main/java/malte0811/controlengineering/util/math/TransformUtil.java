package malte0811.controlengineering.util.math;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

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

    public static void shear(MatrixStack transform, float deltaXPerY, float deltaZPerY) {
        transform.getLast().getMatrix().mul(shear(deltaXPerY, deltaZPerY));
        transform.getLast().getNormal().mul(shearNormal(deltaXPerY, deltaZPerY));
    }
}
