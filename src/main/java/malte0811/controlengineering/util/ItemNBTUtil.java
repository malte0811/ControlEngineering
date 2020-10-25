package malte0811.controlengineering.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;

public class ItemNBTUtil {
    public static Optional<CompoundNBT> getTag(ItemStack stack) {
        return Optional.ofNullable(stack.getTag());
    }
}
