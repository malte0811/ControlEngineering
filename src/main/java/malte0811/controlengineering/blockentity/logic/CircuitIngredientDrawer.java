package malte0811.controlengineering.blockentity.logic;

import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Objects;

public class CircuitIngredientDrawer {
    private static final int CAPACITY = 64;

    private final Tag<Item> filter;
    private final String emptyKey;
    private ItemStack stored = ItemStack.EMPTY;

    public CircuitIngredientDrawer(Tag<Item> filter, String emptyKey) {
        this.filter = filter;
        this.emptyKey = emptyKey;
    }

    public InteractionResult interact(UseOnContext ctx) {
        final ItemStack held = ctx.getItemInHand();
        if (held.is(filter) && canCombine(stored, held)) {
            if (!ctx.getLevel().isClientSide) {
                final int toAdd = Math.min(held.getCount(), CAPACITY - stored.getCount());
                if (stored.isEmpty()) {
                    stored = held.copy();
                    stored.setCount(toAdd);
                } else {
                    stored.grow(toAdd);
                }
                held.shrink(toAdd);
            }
            return InteractionResult.SUCCESS;
        } else if (!stored.isEmpty() && ctx.getPlayer() != null) {
            if (!ctx.getLevel().isClientSide) {
                ItemUtil.giveOrDrop(ctx.getPlayer(), stored);
                stored = ItemStack.EMPTY;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
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

    public void read(CompoundTag nbt) {
        stored = ItemStack.of(nbt);
    }

    public CompoundTag write() {
        return stored.save(new CompoundTag());
    }

    public String getEmptyKey() {
        return emptyKey;
    }
}
