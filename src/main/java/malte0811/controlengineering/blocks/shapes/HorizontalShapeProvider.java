package malte0811.controlengineering.blocks.shapes;

import malte0811.controlengineering.util.Matrix4;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class HorizontalShapeProvider extends CachedShape<Direction> {
    private final VoxelShape baseShape;

    public HorizontalShapeProvider(FromBlockFunction<Direction> getKey, VoxelShape baseShape) {
        super(getKey);
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
}
