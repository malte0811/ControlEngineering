package malte0811.controlengineering.blockentity.base;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface IHasMaster<T extends BlockEntity> extends IHasMasterBase {
    @Nullable
    T computeMasterBE(BlockState stateHere);

    default T getOrComputeMasterBE(BlockState stateHere) {
        if (getCachedMaster() != null) {
            return (T) getCachedMaster();
        } else {
            return computeMasterBE(stateHere);
        }
    }
}
