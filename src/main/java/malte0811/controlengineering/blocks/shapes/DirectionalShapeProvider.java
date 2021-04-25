package malte0811.controlengineering.blocks.shapes;

import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

import java.util.List;

public class DirectionalShapeProvider extends CachedShape<Direction> {
    private final FromBlockFunction<Direction> getKey;
    private final VoxelShape baseShape;

    public DirectionalShapeProvider(FromBlockFunction<Direction> getKey, VoxelShape baseShape) {
        this.getKey = getKey;
        this.baseShape = baseShape;
    }

    @Override
    protected VoxelShape compute(Direction k) {
        List<AxisAlignedBB> boxesIn = baseShape.toBoundingBoxList();
        VoxelShape rotated = VoxelShapes.empty();
        Matrix4 mat = new Matrix4(k);
        for (AxisAlignedBB original : boxesIn) {
            Vector3d minOld = new Vector3d(original.minX, original.minY, original.minZ);
            Vector3d maxOld = new Vector3d(original.maxX, original.maxY, original.maxZ);
            Vector3d firstNew = mat.apply(minOld);
            Vector3d secondNew = mat.apply(maxOld);
            VoxelShape rotatedBox = VoxelShapes.create(new AxisAlignedBB(firstNew, secondNew));
            rotated = VoxelShapes.combine(rotated, rotatedBox, IBooleanFunction.OR);
        }
        return rotated.simplify();
    }

    @Override
    protected Direction getKey(BlockState state, IBlockReader world, BlockPos pos) {
        return getKey.apply(state, world, pos);
    }
}
