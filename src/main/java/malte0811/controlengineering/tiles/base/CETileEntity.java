package malte0811.controlengineering.tiles.base;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

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
}
