package malte0811.controlengineering.blockentity.logic;

import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.LambdaMutable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.Objects;
import java.util.function.Consumer;

public class CircuitIngredientDrawer {
    private static final int CAPACITY = 256;
    private static final String ITEM_KEY = "storedType";
    private static final String COUNT_KEY = "storedCount";

    private final TagKey<Item> filter;
    private final String emptyKey;
    private ItemStack storedType = ItemStack.EMPTY;
    private int storedCount = 0;

    public CircuitIngredientDrawer(TagKey<Item> filter, String emptyKey) {
        this.filter = filter;
        this.emptyKey = emptyKey;
    }

    public ItemInteractionResult interact(UseOnContext ctx) {
        final ItemStack held = ctx.getItemInHand();
        if (held.is(filter) && ItemStack.isSameItemSameComponents(storedType, held)) {
            if (!ctx.getLevel().isClientSide) {
                final int toAdd = Math.min(held.getCount(), CAPACITY - storedCount);
                if (storedType.isEmpty()) {
                    storedType = held.copy();
                    storedCount = toAdd;
                } else {
                    storedCount += toAdd;
                }
                held.shrink(toAdd);
            }
            return ItemInteractionResult.SUCCESS;
        } else if (!storedType.isEmpty() && ctx.getPlayer() != null) {
            if (!ctx.getLevel().isClientSide) {
                final int count = Math.min(this.storedCount, this.storedType.getMaxStackSize());
                ItemUtil.giveOrDrop(ctx.getPlayer(), this.storedType.copyWithCount(count));
                consume(count);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public boolean canConsume(int required) {
        return storedCount >= required;
    }

    public void consume(int required) {
        storedCount -= required;
        if (storedCount <= 0) {
            clear();
        }
    }

    public Mutable<BigItemStack> getStoredRef() {
        return new LambdaMutable<>(this::getStored, newContent -> {
            this.storedType = newContent.type();
            this.storedCount = newContent.count();
        });
    }

    public BigItemStack getStored() {
        return new BigItemStack(storedType.copy(), storedCount);
    }

    public void clear() {
        storedType = ItemStack.EMPTY;
        storedCount = 0;
    }

    public void read(CompoundTag nbt, HolderLookup.Provider provider) {
        this.storedType = ItemStack.parseOptional(provider, nbt.getCompound(ITEM_KEY));
        this.storedCount = nbt.getInt(COUNT_KEY);
    }

    public CompoundTag write(HolderLookup.Provider provider) {
        var result = new CompoundTag();
        result.put(ITEM_KEY, storedType.save(provider));
        result.putInt(COUNT_KEY, storedCount);
        return result;
    }

    public String getEmptyKey() {
        return emptyKey;
    }

    public void drop(Consumer<ItemStack> dropper) {
        while (this.storedCount > 0) {
            final int count = Math.min(this.storedCount, this.storedType.getMaxStackSize());
            dropper.accept(this.storedType.copyWithCount(count));
            consume(count);
        }
        clear();
    }

    public record BigItemStack(ItemStack type, int count) {
        public static final BigItemStack EMPTY = new BigItemStack(ItemStack.EMPTY, 0);
    }
}
