package malte0811.controlengineering.util.math;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

public class MatrixUtils {
    private static final Matrix4f MIRROR = new Matrix4f()
            .translate(0.5F, 0, 0)
            .scale(-1, 1, 1)
            .translate(-0.5F, 0, 0);

    public static Vec3 transform(Matrix4fc transform, double x, double y, double z) {
        Vector4f vec = new Vector4f((float) x, (float) y, (float) z, 1);
        vec.mul(transform);
        vec.mul(1 / vec.w);
        return new Vec3(vec.x(), vec.y(), vec.z());
    }

    public static Vec3 transform(Matrix4fc transform, Vec3 in) {
        return transform(transform, in.x, in.y, in.z);
    }

    public static void rotateFacing(Matrix4f mat, Direction facing, float factor) {
        if (facing == Direction.NORTH) {
            return;
        }
        switch (facing) {
            case UP -> mat.rotationX(factor * Mth.HALF_PI);
            case DOWN -> mat.rotateX(-factor * Mth.HALF_PI);
            case SOUTH -> mat.rotateY(factor * Mth.PI);
            case EAST -> mat.rotateY(-factor * Mth.HALF_PI);
            case WEST -> mat.rotateY(factor * Mth.HALF_PI);
        }
    }


    public static ClipContext transformRay(Matrix4fc mat, Vec3 start, Vec3 end) {
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

    public static Matrix4f inverseFacing(Direction facing, boolean mirror) {
        var result = inverseFacing(facing);
        if (mirror) {
            result.mul(MIRROR);
        }
        return result;
    }

    public static Matrix4f facing(Direction facing) {
        return facing(facing, 1);
    }

    private static Matrix4f facing(Direction facing, float factor) {
        var ret = new Matrix4f()
                .translate(.5f, .5f, .5f);
        rotateFacing(ret, facing, factor);
        ret.translate(-.5f, -.5f, -.5f);
        return ret;
    }
}
