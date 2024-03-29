package malte0811.controlengineering.util;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ItemUtil {
    public static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        } else  {
            player.inventoryMenu.broadcastChanges();
        }
    }

    public static boolean tryConsumeItemsFrom(
            List<IngredientWithSize> toConsume, Collection<CapabilityReference<IItemHandler>> sources
    ) {
        return tryConsumeItemsFrom(toConsume, sources, true) && tryConsumeItemsFrom(toConsume, sources, false);
    }

    public static boolean tryConsumeItemsFrom(
            List<IngredientWithSize> toConsume, Collection<CapabilityReference<IItemHandler>> sources, boolean simulate
    ) {
        List<MutablePair<Ingredient, Integer>> missing = new ArrayList<>(toConsume.size());
        for (IngredientWithSize ingred : toConsume) {
            missing.add(MutablePair.of(ingred.getBaseIngredient(), ingred.getCount()));
        }
        for (CapabilityReference<IItemHandler> handlerRef : sources) {
            final IItemHandler handler = handlerRef.getNullable();
            if (handler == null) {
                continue;
            }
            for (int slot = 0; slot < handler.getSlots(); ++slot) {
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (!inSlot.isEmpty()) {
                    int sizeRemaining = inSlot.getCount();
                    Iterator<MutablePair<Ingredient, Integer>> iterator = missing.iterator();
                    while (iterator.hasNext()) {
                        MutablePair<Ingredient, Integer> entry = iterator.next();
                        if (entry.getLeft().test(inSlot)) {
                            int consume = Math.min(sizeRemaining, entry.getRight());
                            entry.right -= consume;
                            sizeRemaining -= consume;
                            if (entry.right == 0) {
                                iterator.remove();
                            }
                            if (sizeRemaining == 0) {
                                break;
                            }
                        }
                    }
                    var numToExtract = inSlot.getCount() - sizeRemaining;
                    var extracted = handler.extractItem(slot, numToExtract, simulate);
                    if (extracted.getCount() != numToExtract) {
                        return false;
                    } else if (missing.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
