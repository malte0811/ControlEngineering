package malte0811.controlengineering.tiles.base;

import net.minecraft.core.BlockPos;
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

public abstract class CETileEntity extends BlockEntity implements IHasMasterBase {
    private BlockEntity cachedMaster;

    public CETileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public void setCachedMaster(BlockEntity newCachedMaster) {
        cachedMaster = newCachedMaster;
    }

    @Override
    public BlockEntity getCachedMaster() {
        return cachedMaster;
    }

    protected void writeSyncedData(CompoundTag out) {}

    protected void readSyncedData(CompoundTag in) {}

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readSyncedData(tag);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        var updateTag = super.getUpdateTag();
        writeSyncedData(updateTag);
        return updateTag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(
                this,
                be -> {
                    CompoundTag result = new CompoundTag();
                    ((CETileEntity) be).writeSyncedData(result);
                    return result;
                }
        );
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        readSyncedData(pkt.getTag());
    }
}
