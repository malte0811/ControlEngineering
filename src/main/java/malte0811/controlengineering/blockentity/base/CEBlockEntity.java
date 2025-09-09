package malte0811.controlengineering.blockentity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CEBlockEntity extends BlockEntity implements IHasMasterBase {
    private BlockEntity cachedMaster;

    public CEBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setCachedMaster(BlockEntity newCachedMaster) {
        cachedMaster = newCachedMaster;
    }

    @Override
    public BlockEntity getCachedMaster() {
        return cachedMaster;
    }

    protected void writeSyncedData(CompoundTag out, HolderLookup.Provider provider) { }

    protected void readSyncedData(CompoundTag in, HolderLookup.Provider provider) { }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        readSyncedData(tag, provider);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var updateTag = super.getUpdateTag(provider);
        writeSyncedData(updateTag, provider);
        return updateTag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(
                this,
                (be, lookup) -> {
                    CompoundTag result = new CompoundTag();
                    ((CEBlockEntity) be).writeSyncedData(result, lookup);
                    return result;
                }
        );
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        readSyncedData(pkt.getTag(), lookupProvider);
    }
}
