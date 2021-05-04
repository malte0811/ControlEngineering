package malte0811.controlengineering.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;

public class TileUtil {
    public static void markDirtyAndSync(TileEntity tile) {
        tile.markDirty();
        World world = tile.getWorld();
        if (world != null && world.getChunkProvider() instanceof ServerChunkProvider) {
            ((ServerChunkProvider) world.getChunkProvider()).markBlockChanged(tile.getPos());
        }
    }
}
