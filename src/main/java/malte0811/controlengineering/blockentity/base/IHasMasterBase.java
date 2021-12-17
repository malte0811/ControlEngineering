package malte0811.controlengineering.blockentity.base;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IHasMasterBase {
    BlockEntity getCachedMaster();

    void setCachedMaster(BlockEntity newCachedMaster);
}
