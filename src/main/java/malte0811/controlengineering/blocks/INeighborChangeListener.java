package malte0811.controlengineering.blocks;

import net.minecraft.util.math.BlockPos;

public interface INeighborChangeListener {
    void onNeighborChanged(BlockPos neighbor);
}
