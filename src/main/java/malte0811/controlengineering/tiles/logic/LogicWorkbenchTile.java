package malte0811.controlengineering.tiles.logic;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.gui.logic.LogicDesignScreen;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class LogicWorkbenchTile extends TileEntity implements SelectionShapeOwner {
    private Schematic schematic = new Schematic();

    public LogicWorkbenchTile() {
        super(CETileEntities.LOGIC_WORKBENCH.get());
    }

    private final CachedValue<BlockState, SelectionShapes> shapes = new CachedValue<>(
            this::getBlockState,
            state -> {
                LogicWorkbenchBlock.Offset offset = state.get(LogicWorkbenchBlock.OFFSET);
                VoxelShape baseShape = LogicWorkbenchBlock.SHAPE.apply(offset, state.get(LogicWorkbenchBlock.FACING));
                return new SingleShape(baseShape, ctx -> {
                    BlockPos origin = CEBlocks.LOGIC_WORKBENCH.get().getMainBlock(state, this);
                    TileEntity atOrigin = world.getTileEntity(origin);
                    if (atOrigin instanceof LogicWorkbenchTile) {
                        return ((LogicWorkbenchTile) atOrigin).handleMainClick(ctx);
                    } else {
                        return ActionResultType.FAIL;
                    }
                });
            }
    );

    @Override
    public SelectionShapes getShape() {
        return shapes.get();
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound.put("schematic", Codecs.encode(Schematic.CODEC, schematic));
        return super.write(compound);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        schematic = Codecs.readOrNull(Schematic.CODEC, nbt.get("schematic"));
        if (schematic == null) {
            schematic = new Schematic();
        }
    }

    private ActionResultType handleMainClick(ItemUseContext ctx) {
        if (world.isRemote) {
            //TODO open on server, implement sync
            Minecraft.getInstance().displayGuiScreen(new LogicDesignScreen(schematic));
        }
        return ActionResultType.SUCCESS;
    }
}
