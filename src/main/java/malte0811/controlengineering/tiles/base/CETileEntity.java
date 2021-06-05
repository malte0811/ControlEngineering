package malte0811.controlengineering.tiles.base;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CETileEntity extends TileEntity implements IHasMasterBase {
    private TileEntity cachedMaster;

    public CETileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void setCachedMaster(TileEntity newCachedMaster) {
        cachedMaster = newCachedMaster;
    }

    @Override
    public TileEntity getCachedMaster() {
        return cachedMaster;
    }

    protected CompoundNBT writeSyncedData(CompoundNBT out) {
        return out;
    }

    protected void readSyncedData(CompoundNBT in) {}

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        readSyncedData(tag);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return writeSyncedData(super.getUpdateTag());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT data = writeSyncedData(new CompoundNBT());
        if (data.isEmpty()) {
            return super.getUpdatePacket();
        } else {
            return new SUpdateTileEntityPacket(pos, -1, writeSyncedData(new CompoundNBT()));
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        readSyncedData(pkt.getNbtCompound());
    }
}
