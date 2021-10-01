package malte0811.controlengineering.util;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileUtil {
    public static void markDirtyAndSync(BlockEntity tile) {
        tile.setChanged();
        Level world = tile.getLevel();
        if (world != null && world.getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.blockChanged(tile.getBlockPos());
        }
    }
}
