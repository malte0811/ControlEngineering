package malte0811.controlengineering.util.math;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

public class MatrixUtils {
    public static Vec3 transform(Matrix4f transform, double x, double y, double z) {
        Vector4f vec = new Vector4f((float) x, (float) y, (float) z, 1);
        vec.transform(transform);
        vec.perspectiveDivide();
        return new Vec3(vec.x(), vec.y(), vec.z());
    }

    public static Vec3 transform(Matrix4f transform, Vec3 in) {
        return transform(transform, in.x, in.y, in.z);
    }

    public static void rotateFacing(Matrix4f mat, Direction facing, float factor) {
        if (facing == Direction.NORTH) {
            return;
        }
        switch (facing) {
            case UP -> mat.multiply(new Quaternion(factor * Mth.HALF_PI, 0, 0, false));
            case DOWN -> mat.multiply(new Quaternion(-factor * Mth.HALF_PI, 0, 0, false));
            case SOUTH -> mat.multiply(new Quaternion(0, factor * Mth.PI, 0, false));
            case EAST -> mat.multiply(new Quaternion(0, -factor * Mth.HALF_PI, 0, false));
            case WEST -> mat.multiply(new Quaternion(0, factor * Mth.HALF_PI, 0, false));
        }
    }


    public static ClipContext transformRay(Matrix4f mat, Vec3 start, Vec3 end) {
        return new ClipContext(
                transform(mat, start),
                transform(mat, end),
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                null
        );
    }

    public static Matrix4f inverseFacing(Direction facing) {
        return facing(facing, -1);
    }

    public static Matrix4f facing(Direction facing) {
        return facing(facing, 1);
    }

    private static Matrix4f facing(Direction facing, float factor) {
        var ret = new Matrix4f();
        ret.setIdentity();
        ret.multiplyWithTranslation(.5f, .5f, .5f);
        rotateFacing(ret, facing, factor);
        ret.multiplyWithTranslation(-.5f, -.5f, -.5f);
        return ret;
    }
}
