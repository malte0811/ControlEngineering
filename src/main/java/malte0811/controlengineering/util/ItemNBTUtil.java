package malte0811.controlengineering.util;

import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ItemNBTUtil {
    public static Optional<CompoundTag> getTag(ItemStack stack) {
        return Optional.ofNullable(stack.getTag());
    }
}
