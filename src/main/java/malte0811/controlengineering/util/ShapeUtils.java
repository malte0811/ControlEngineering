package malte0811.controlengineering.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collection;
import java.util.function.Function;
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
}
