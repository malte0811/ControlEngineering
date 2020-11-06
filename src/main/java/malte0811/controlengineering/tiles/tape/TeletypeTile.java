package malte0811.controlengineering.tiles.tape;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.tiles.CETileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TeletypeTile extends TileEntity {
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
}
