package malte0811.controlengineering.tiles.base;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IHasMaster extends IHasMasterBase {
    @Nullable
    BlockEntity computeMasterTile(BlockState stateHere);

    default BlockEntity getOrComputeMasterTile(BlockState stateHere) {
        if (getCachedMaster() != null) {
            return getCachedMaster();
        } else {
            return computeMasterTile(stateHere);
        }
    }
}
