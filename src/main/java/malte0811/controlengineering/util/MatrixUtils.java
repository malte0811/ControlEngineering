package malte0811.controlengineering.util;

import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

public class MatrixUtils {
    public static Vector3d transform(Vector3d in, TransformationMatrix transform) {
        Vector4f vec = new Vector4f((float) in.x, (float) in.y, (float) in.z, 1);
        transform.transformPosition(vec);
        vec.perspectiveDivide();
        return new Vector3d(vec.getX(), vec.getY(), vec.getZ());
    }
}
