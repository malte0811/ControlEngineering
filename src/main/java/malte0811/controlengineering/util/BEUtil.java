package malte0811.controlengineering.util;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BEUtil {
    public static void markDirtyAndSync(BlockEntity bEntity) {
        bEntity.setChanged();
        Level world = bEntity.getLevel();
        if (world != null && world.getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.blockChanged(bEntity.getBlockPos());
        }
    }
}
