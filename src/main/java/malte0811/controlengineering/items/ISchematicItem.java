package malte0811.controlengineering.items;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

// Marker interface, mostly for the copying recipe
public interface ISchematicItem {
    String SCHEMATIC_KEY = "schematic";

    static Schematic getSchematic(ItemStack stack) {
        Preconditions.checkArgument(stack.getItem() instanceof ISchematicItem);
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        return Schematic.CODEC.fromNBT(tag.get(SCHEMATIC_KEY));
    }

    static ItemStack create(Supplier<? extends Item> item, Schematic schematic) {
        ItemStack result = item.get().getDefaultInstance();
        if (!schematic.isEmpty()) {
            result.getOrCreateTag().put(SCHEMATIC_KEY, Schematic.CODEC.toNBT(schematic));
        }
        return result;
    }
}
