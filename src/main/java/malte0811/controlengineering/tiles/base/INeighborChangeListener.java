package malte0811.controlengineering.tiles.base;

import net.minecraft.util.math.BlockPos;

public interface INeighborChangeListener {
    void onNeighborChanged(BlockPos neighbor);
}
