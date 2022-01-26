package malte0811.controlengineering.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemNBTUtil {
    public static Optional<CompoundTag> getTag(ItemStack stack) {
        return Optional.ofNullable(stack.getTag());
    }
}
