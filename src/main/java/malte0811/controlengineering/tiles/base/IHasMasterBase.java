package malte0811.controlengineering.tiles.base;

import net.minecraft.tileentity.TileEntity;

public interface IHasMasterBase {
    TileEntity getCachedMaster();

    void setCachedMaster(TileEntity newCachedMaster);
}
