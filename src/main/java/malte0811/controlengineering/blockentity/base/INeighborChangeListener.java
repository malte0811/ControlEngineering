package malte0811.controlengineering.blockentity.base;

import net.minecraft.core.BlockPos;

public interface INeighborChangeListener {
    //TODO CALL!
    void onNeighborChanged(BlockPos neighbor);
}
