package malte0811.controlengineering.tiles.logic;

import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResultType;

import java.util.Objects;

public class CircuitIngredientDrawer {
    private static final int CAPACITY = 64;

    private final ITag<Item> filter;
    private ItemStack stored = ItemStack.EMPTY;

    public CircuitIngredientDrawer(ITag<Item> filter) {
        this.filter = filter;
    }

    public ActionResultType interact(ItemUseContext ctx) {
        final ItemStack held = ctx.getItem();
        if (held.getItem().isIn(filter) && canCombine(stored, held)) {
            final int toAdd = Math.min(held.getCount(), CAPACITY - stored.getCount());
            if (stored.isEmpty()) {
                stored = held.copy();
                stored.setCount(toAdd);
            } else {
                stored.grow(toAdd);
            }
            held.shrink(toAdd);
            return ActionResultType.SUCCESS;
        } else if (!stored.isEmpty() && ctx.getPlayer() != null) {
            ItemUtil.giveOrDrop(ctx.getPlayer(), stored);
            stored = ItemStack.EMPTY;
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    private static boolean canCombine(ItemStack existing, ItemStack added) {
        if (existing.isEmpty()) {
            return true;
        } else if (added.getItem() != existing.getItem()) {
            return false;
        } else if (!existing.areCapsCompatible(added)) {
            return false;
        } else {
            return Objects.equals(added.getTag(), existing.getTag());
        }
    }

    public boolean canConsume(int required) {
        return stored.getCount() >= required;
    }

    public void consume(int required) {
        stored.shrink(required);
    }

    public ItemStack getStored() {
        return stored;
    }

    public void clear() {
        stored = ItemStack.EMPTY;
    }

    public void read(CompoundNBT nbt) {
        stored = ItemStack.read(nbt);
    }

    public CompoundNBT write() {
        return stored.write(new CompoundNBT());
    }
}
