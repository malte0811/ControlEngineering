package malte0811.controlengineering.items;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.itemdata.CEDataComponents;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

// Marker interface, mostly for the copying recipe
public interface ISchematicItem {
    static Schematic getSchematic(ItemStack stack) {
        return stack.get(CEDataComponents.SCHEMATIC);
    }

    static <T extends Item & ISchematicItem>
    ItemStack create(Supplier<T> item, Schematic schematic) {
        ItemStack result = item.get().getDefaultInstance();
        if (!schematic.isEmpty()) {
            result.set(CEDataComponents.SCHEMATIC, schematic);
        }
        return result;
    }
}
