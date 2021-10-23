package malte0811.controlengineering.blocks.shapes;

import com.mojang.math.Matrix4f;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
        List<AABB> boxesIn = baseShape.toAabbs();
        VoxelShape rotated = Shapes.empty();
        Matrix4f mat = MatrixUtils.facing(k);
        for (AABB original : boxesIn) {
            Vec3 minOld = new Vec3(original.minX, original.minY, original.minZ);
            Vec3 maxOld = new Vec3(original.maxX, original.maxY, original.maxZ);
            Vec3 firstNew = MatrixUtils.transform(mat, minOld);
            Vec3 secondNew = MatrixUtils.transform(mat, maxOld);
            VoxelShape rotatedBox = Shapes.create(new AABB(firstNew, secondNew));
            rotated = Shapes.joinUnoptimized(rotated, rotatedBox, BooleanOp.OR);
        }
        return rotated.optimize();
    }

    @Override
    protected Direction getKey(BlockState state, BlockGetter world, BlockPos pos) {
        return getKey.apply(state, world, pos);
    }
}
