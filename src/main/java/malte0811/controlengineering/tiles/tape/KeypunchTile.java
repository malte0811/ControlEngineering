package malte0811.controlengineering.tiles.tape;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.tiles.base.CETileEntity;
import malte0811.controlengineering.tiles.base.IExtraDropTile;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.math.MatrixUtils;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class KeypunchTile extends CETileEntity implements SelectionShapeOwner, IExtraDropTile {
    public static final VoxelShape INPUT_SHAPE = createPixelRelative(11, 6, 2, 15, 9, 4);
    public static final VoxelShape OUTPUT_SHAPE = createPixelRelative(2, 6, 1, 6, 10, 5);

    private KeypunchState state = new KeypunchState();

    public KeypunchTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        state = Codecs.readOptional(KeypunchState.CODEC, nbt.get("state")).orElseGet(KeypunchState::new);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("state", Codecs.encode(KeypunchState.CODEC, state));
    }

    private final CachedValue<Direction, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> getBlockState().getValue(KeypunchBlock.FACING), f -> createSelectionShapes(f, this)
    );

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    private static SelectionShapes createSelectionShapes(Direction d, KeypunchTile tile) {
        List<SelectionShapes> subshapes = new ArrayList<>(2);
        // Punched tape output
        subshapes.add(new SingleShape(
                OUTPUT_SHAPE, ctx -> tile.getState().removeWrittenTape(ctx.getPlayer())
        ));
        // Add clear tape to input/take it from input
        subshapes.add(new SingleShape(
                INPUT_SHAPE, ctx -> tile.getState().removeOrAddClearTape(ctx.getPlayer(), ctx.getItemInHand())
        ));
        return new ListShapes(
                KeypunchBlock.SHAPE_PROVIDER.apply(d),
                MatrixUtils.inverseFacing(d),
                subshapes,
                ctx -> {
                    CEBlocks.KEYPUNCH.get().openContainer(
                            ctx.getPlayer(), tile.getBlockState(), ctx.getLevel(), ctx.getClickedPos()
                    );
                    return InteractionResult.SUCCESS;
                }
        );
    }

    public KeypunchState getState() {
        return state;
    }

    @Override
    public void getExtraDrops(Consumer<ItemStack> dropper) {
        if (state.getAvailable() > 0) {
            dropper.accept(EmptyTapeItem.withLength(state.getAvailable()));
        }
        if (!state.getData().isEmpty() || state.getErased() > 0) {
            byte[] bytes = new byte[state.getData().size() + state.getErased()];
            System.arraycopy(state.getData().toByteArray(), 0, bytes, 0, state.getData().size());
            Arrays.fill(bytes, state.getData().size(), bytes.length, BitUtils.fixParity((byte) 0xff));
            dropper.accept(PunchedTapeItem.withBytes(bytes));
        }
    }
}
