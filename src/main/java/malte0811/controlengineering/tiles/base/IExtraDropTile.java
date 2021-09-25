package malte0811.controlengineering.tiles.base;

import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;

public interface IExtraDropTile {
    void getExtraDrops(Consumer<ItemStack> dropper);
}
