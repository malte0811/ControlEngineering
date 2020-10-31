package malte0811.controlengineering.tiles.tape;

import malte0811.controlengineering.tiles.CETileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;

public class TeletypeTile extends TileEntity {
    private static final String WRITTEN_KEY = "writtenBytes";
    private static final String AVAILABLE_KEY = "availableKey";
    private byte[] written = new byte[0];
    private int available = 0;

    public TeletypeTile() {
        super(CETileEntities.TELETYPE.get());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
        super.read(state, nbt);
        written = nbt.getByteArray(WRITTEN_KEY);
        available = nbt.getInt(AVAILABLE_KEY);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        compound = super.write(compound);
        compound.putByteArray(WRITTEN_KEY, written);
        compound.putInt(AVAILABLE_KEY, available);
        return compound;
    }
}
