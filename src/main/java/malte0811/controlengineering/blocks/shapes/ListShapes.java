package malte0811.controlengineering.blocks.shapes;

import malte0811.controlengineering.util.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ListShapes extends SelectionShapes {
    private final VoxelShape mainShape;
    private final Matrix4 outerToInner;
    private final List<? extends SelectionShapes> shapes;
    private final Function<ItemUseContext, ActionResultType> onClick;

    public ListShapes(
            VoxelShape mainShape,
            Matrix4 outerToInner,
            List<? extends SelectionShapes> shapes,
            Function<ItemUseContext, ActionResultType> onClick
    ) {
        this.mainShape = mainShape;
        this.outerToInner = outerToInner;
        this.shapes = shapes;
        this.onClick = onClick;
    }

    @Nullable
    @Override
    public VoxelShape mainShape() {
        return mainShape;
    }

    @Nonnull
    @Override
    public Matrix4 outerToInnerPosition() {
        return outerToInner;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return shapes;
    }

    @Override
    public ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType) {
        if (defaultType == ActionResultType.PASS) {
            return onClick.apply(ctx);
        } else {
            return defaultType;
        }
    }
}
