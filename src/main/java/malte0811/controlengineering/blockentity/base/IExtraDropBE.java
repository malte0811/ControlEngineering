package malte0811.controlengineering.blockentity.base;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface IExtraDropBE {
    void getExtraDrops(Consumer<ItemStack> dropper);
}
