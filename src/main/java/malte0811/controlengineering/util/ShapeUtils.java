package malte0811.controlengineering.util;

import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ShapeUtils {
    public static VoxelShape or(Stream<AxisAlignedBB> parts) {
        return parts.map(VoxelShapes::create)
                .reduce(VoxelShapes.empty(), VoxelShapes::or);
    }

    public static AxisAlignedBB transform(Matrix4 matrix, AxisAlignedBB transform) {
        return transformFunc(matrix).apply(transform);
    }

    public static UnaryOperator<AxisAlignedBB> transformFunc(Matrix4 transform) {
        return aabb -> new AxisAlignedBB(
                transform.apply(new Vector3d(aabb.minX, aabb.minY, aabb.minZ)),
                transform.apply(new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ))
        );
    }

    public static VoxelShape createPixelRelative(double x1, double y1, double z1, double x2, double y2, double z2) {
        return VoxelShapes.create(x1 / 16., y1 / 16., z1 / 16., x2 / 16., y2 / 16., z2 / 16.);
    }
}
