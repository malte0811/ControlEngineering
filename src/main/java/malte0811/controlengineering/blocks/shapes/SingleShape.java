package malte0811.controlengineering.blocks.shapes;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.util.Matrix4;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SingleShape extends SelectionShapes {
    public static final SelectionShapes FULL_BLOCK = new SingleShape(
            VoxelShapes.fullCube(),
            $ -> ActionResultType.PASS
    );

    private final VoxelShape mainShape;
    private final Function<ItemUseContext, ActionResultType> onClick;

    public SingleShape(VoxelShape mainShape, Function<ItemUseContext, ActionResultType> onClick) {
        this.mainShape = mainShape;
        this.onClick = onClick;
    }

    @Override
    public @Nullable
    VoxelShape mainShape() {
        return this.mainShape;
    }

    @Nonnull
    @Override
    public Matrix4 outerToInnerPosition() {
        return Matrix4.IDENTITY;
    }

    @Nonnull
    @Override
    public List<? extends SelectionShapes> innerShapes() {
        return ImmutableList.of();
    }

    @Override
    public ActionResultType onUse(ItemUseContext ctx, ActionResultType defaultType) {
        return onClick.apply(ctx);
    }
}
