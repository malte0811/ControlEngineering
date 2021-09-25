package malte0811.controlengineering.util;

import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ShapeUtils {
    public static VoxelShape or(Stream<AABB> parts) {
        return parts.map(Shapes::create)
                .reduce(Shapes.empty(), Shapes::or);
    }

    public static AABB transform(Matrix4 matrix, AABB transform) {
        return transformFunc(matrix).apply(transform);
    }

    public static UnaryOperator<AABB> transformFunc(Matrix4 transform) {
        return aabb -> new AABB(
                transform.apply(new Vec3(aabb.minX, aabb.minY, aabb.minZ)),
                transform.apply(new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ))
        );
    }

    public static VoxelShape createPixelRelative(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Shapes.box(x1 / 16., y1 / 16., z1 / 16., x2 / 16., y2 / 16., z2 / 16.);
    }
}
