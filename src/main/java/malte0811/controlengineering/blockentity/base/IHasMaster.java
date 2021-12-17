package malte0811.controlengineering.blockentity.base;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IHasMaster extends IHasMasterBase {
    @Nullable
    BlockEntity computeMasterBE(BlockState stateHere);

    default BlockEntity getOrComputeMasterBE(BlockState stateHere) {
        if (getCachedMaster() != null) {
            return getCachedMaster();
        } else {
            return computeMasterBE(stateHere);
        }
    }
}
