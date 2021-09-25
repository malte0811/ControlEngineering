package malte0811.controlengineering.tiles.base;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface IHasMasterBase {
    BlockEntity getCachedMaster();

    void setCachedMaster(BlockEntity newCachedMaster);
}
