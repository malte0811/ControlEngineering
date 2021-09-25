package malte0811.controlengineering.tiles.base;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CETileEntity extends BlockEntity implements IHasMasterBase {
    private BlockEntity cachedMaster;

    public CETileEntity(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void setCachedMaster(BlockEntity newCachedMaster) {
        cachedMaster = newCachedMaster;
    }

    @Override
    public BlockEntity getCachedMaster() {
        return cachedMaster;
    }

    protected CompoundTag writeSyncedData(CompoundTag out) {
        return out;
    }

    protected void readSyncedData(CompoundTag in) {}

    @Override
    public void handleUpdateTag(BlockState state, CompoundTag tag) {
        super.handleUpdateTag(state, tag);
        readSyncedData(tag);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return writeSyncedData(super.getUpdateTag());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag data = writeSyncedData(new CompoundTag());
        if (data.isEmpty()) {
            return super.getUpdatePacket();
        } else {
            return new ClientboundBlockEntityDataPacket(worldPosition, -1, writeSyncedData(new CompoundTag()));
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        readSyncedData(pkt.getTag());
    }
}
