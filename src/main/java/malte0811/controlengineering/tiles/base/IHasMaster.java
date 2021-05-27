package malte0811.controlengineering.tiles.base;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface IHasMaster extends IHasMasterBase {
    @Nullable
    TileEntity computeMasterTile(BlockState stateHere);

    default TileEntity getOrComputeMasterTile(BlockState stateHere) {
        if (getCachedMaster() != null) {
            return getCachedMaster();
        } else {
            return computeMasterTile(stateHere);
        }
    }
}
