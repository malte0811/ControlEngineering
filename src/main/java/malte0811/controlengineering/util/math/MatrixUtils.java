package malte0811.controlengineering.util.math;

import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.world.phys.Vec3;

public class MatrixUtils {
    public static Vec3 transform(Vec3 in, Transformation transform) {
        Vector4f vec = new Vector4f((float) in.x, (float) in.y, (float) in.z, 1);
        transform.transformPosition(vec);
        vec.perspectiveDivide();
        return new Vec3(vec.x(), vec.y(), vec.z());
    }
}
