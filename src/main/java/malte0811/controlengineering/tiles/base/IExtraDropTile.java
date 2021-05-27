package malte0811.controlengineering.tiles.base;

import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

public interface IExtraDropTile {
    void getExtraDrops(Consumer<ItemStack> dropper);
}
