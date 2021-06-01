package malte0811.controlengineering.tiles.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock.Offset;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.tiles.base.CETileEntity;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.math.Matrix4;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PanelDesignerTile extends CETileEntity implements SelectionShapeOwner {
    private static final Codec<List<PlacedComponent>> COMPONENTS_CODEC = Codec.list(PlacedComponent.CODEC);

    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                Offset offset = state.get(PanelDesignerBlock.OFFSET);
                Direction facing = state.get(PanelDesignerBlock.FACING);
                VoxelShape baseShape = PanelDesignerBlock.SHAPE.apply(offset, facing);
                if (offset == Offset.ORIGIN) {
                    Function<ItemUseContext, ActionResultType> openUI = makeInteraction(
                            state, PanelDesignerTile::openUI
                    );
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.TABLE_TOP, openUI)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else if (offset == Offset.BACK_TOP) {
                    Function<ItemUseContext, ActionResultType> writeTape = makeInteraction(
                            state, PanelDesignerTile::writeTape
                    );
                    Function<ItemUseContext, ActionResultType> addTape = makeInteraction(
                            state, PanelDesignerTile::addTape
                    );
                    Function<ItemUseContext, ActionResultType> takeTape = makeInteraction(
                            state, PanelDesignerTile::takeTape
                    );
                    return new ListShapes(
                            baseShape,
                            Matrix4.inverseFacing(facing),
                            ImmutableList.of(
                                    new SingleShape(PanelDesignerBlock.BUTTON, writeTape),
                                    new SingleShape(TeletypeTile.INPUT_SHAPE, addTape),
                                    new SingleShape(TeletypeTile.OUTPUT_SHAPE, takeTape)
                            ),
                            $ -> ActionResultType.PASS
                    );
                } else {
                    return new SingleShape(baseShape, $ -> ActionResultType.PASS);
                }
            }
    );

    private List<PlacedComponent> components = new ArrayList<>();

    public PanelDesignerTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    private Function<ItemUseContext, ActionResultType> makeInteraction(
            BlockState state, BiFunction<PanelDesignerTile, ItemUseContext, ActionResultType> handler
    ) {
        return ctx -> {
            BlockPos origin = CEBlocks.PANEL_DESIGNER.get().getMainBlock(state, this);
            TileEntity atOrigin = world.getTileEntity(origin);
            if (atOrigin instanceof PanelDesignerTile) {
                return handler.apply((PanelDesignerTile) atOrigin, ctx);
            } else {
                return ActionResultType.FAIL;
            }
        };
    }

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        components = new ArrayList<>(
                Codecs.read(COMPONENTS_CODEC, nbt.get("components")).result().orElse(ImmutableList.of())
        );
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.put("components", Codecs.encode(COMPONENTS_CODEC, components));
        return compound;
    }

    public List<PlacedComponent> getComponents() {
        return components;
    }

    private ActionResultType openUI(ItemUseContext ctx) {
        if (!world.isRemote) {
            CEBlocks.PANEL_DESIGNER.get().openContainer(ctx.getPlayer(), getBlockState(), world, pos);
        }
        return ActionResultType.SUCCESS;
    }

    private ActionResultType writeTape(ItemUseContext ctx) {
        //TODO implement
        return ActionResultType.SUCCESS;
    }

    private ActionResultType addTape(ItemUseContext ctx) {
        //TODO implement
        return ActionResultType.SUCCESS;
    }

    private ActionResultType takeTape(ItemUseContext ctx) {
        //TODO implement
        return ActionResultType.SUCCESS;
    }
}
