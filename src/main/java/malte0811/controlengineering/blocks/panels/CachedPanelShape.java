package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.shapes.CachedShape;
import malte0811.controlengineering.blocks.shapes.FromBlockFunction;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import malte0811.controlengineering.util.ShapeUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

public class CachedPanelShape extends CachedShape<PanelTransform> {
    protected CachedPanelShape() {
        super((state, world, pos) -> {
            ControlPanelTile base = PanelBlock.getBase(world, state, pos);
            if (base != null) {
                return base.getTransform();
            } else {
                return new PanelTransform(.5F, 0, PanelOrientation.DOWN_NORTH);
            }
        });
    }

    @Override
    protected VoxelShape compute(PanelTransform transform) {
        return getPanelShape(transform);
    }

    public static VoxelShape getPanelShape(PanelTransform transform) {
        List<AxisAlignedBB> parts = new ArrayList<>();
        final double frontHeight = Math.max(transform.getCenterHeight(), transform.getFrontHeight());
        final double backHeight = Math.max(transform.getCenterHeight(), transform.getBackHeight());
        parts.add(new AxisAlignedBB(0, 0, 0, 0.5, frontHeight, 1));
        parts.add(new AxisAlignedBB(0.5, 0, 0, 1, backHeight, 1));
        return ShapeUtils.or(
                parts.stream().map(ShapeUtils.transformFunc(transform.getPanelBottomToWorld()))
        );
    }

    public static FromBlockFunction<VoxelShape> create() {
        return FromBlockFunction.either(
                FromBlockFunction.getProperty(PanelBlock.IS_BASE),
                new CachedPanelShape(),
                FromBlockFunction.constant(VoxelShapes.fullCube())
        );
    }
}
