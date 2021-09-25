package malte0811.controlengineering.tiles.base;

import net.minecraft.core.BlockPos;

public interface INeighborChangeListener {
    void onNeighborChanged(BlockPos neighbor);
}
