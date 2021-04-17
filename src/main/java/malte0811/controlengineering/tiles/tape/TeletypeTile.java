package malte0811.controlengineering.tiles.tape;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.blocks.tape.TeletypeBlock;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.util.CachedValue;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.math.Matrix4;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TeletypeTile extends TileEntity implements SelectionShapeOwner {
    private static final String WRITTEN_KEY = "writtenBytes";
    private static final String AVAILABLE_KEY = "available";
    private ByteList written = new ByteArrayList();
    private int available = 20;

    public TeletypeTile() {
        super(CETileEntities.TELETYPE.get());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        written = new ByteArrayList(nbt.getByteArray(WRITTEN_KEY));
        available = nbt.getInt(AVAILABLE_KEY);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.putByteArray(WRITTEN_KEY, written.toByteArray());
        compound.putInt(AVAILABLE_KEY, available);
        return compound;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        //TODO only sync last X bytes?
        return write(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(state, tag);
    }

    public void type(byte[] typed) {
        if (available > 0) {
            int actuallyTyped = Math.min(available, typed.length);
            written.addElements(written.size(), typed, 0, actuallyTyped);
            available -= actuallyTyped;
        }
    }

    public byte[] getTypedBytes() {
        return written.toByteArray();
    }

    public int getRemainingBytes() {
        return available;
    }

    private final CachedValue<Direction, SelectionShapes> selectionShapes = new CachedValue<>(
            () -> getBlockState().get(TeletypeBlock.FACING), f -> createSelectionShapes(f, this)
    );

    @Override
    public SelectionShapes getShape() {
        return selectionShapes.get();
    }

    private static SelectionShapes createSelectionShapes(Direction d, TeletypeTile tile) {
        List<SelectionShapes> subshapes = new ArrayList<>(2);
        // Punched tape output
        subshapes.add(new SingleShape(VoxelShapes.create(2, 6, 1, 6, 10, 5), ctx -> {
            if (!tile.written.isEmpty() && ctx.getPlayer() != null) {
                ItemUtil.giveOrDrop(ctx.getPlayer(), PunchedTapeItem.withBytes(tile.written.toByteArray()));
                tile.written.clear();
                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.FAIL;
            }
        }));
        // Add clear tape to input/take it from input
        subshapes.add(new SingleShape(VoxelShapes.create(11, 6, 2, 15, 9, 4), ctx -> {
            ItemStack item = ctx.getItem();
            final int length = EmptyTapeItem.getLength(item);
            if (length > 0) {
                //TODO limit?
                tile.available += length;
                item.shrink(1);
            } else if (tile.available > 0 && ctx.getPlayer() != null) {
                ItemUtil.giveOrDrop(ctx.getPlayer(), EmptyTapeItem.withLength(tile.available));
                tile.available = 0;
            }
            return ActionResultType.SUCCESS;
        }));
        return new ListShapes(
                TeletypeBlock.SHAPE_PROVIDER.apply(d),
                new Matrix4(d).scale(16, 16, 16),
                subshapes,
                ctx -> {
                    CEBlocks.TELETYPE.get().openContainer(
                            ctx.getPlayer(), tile.getBlockState(), ctx.getWorld(), ctx.getPos()
                    );
                    return ActionResultType.SUCCESS;
                }
        );
    }
}
