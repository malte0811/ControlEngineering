package malte0811.controlengineering.blockentity.logic;

import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.LambdaMutable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.items.ItemHandlerHelper;
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

    public InteractionResult interact(UseOnContext ctx) {
        final ItemStack held = ctx.getItemInHand();
        if (held.is(filter) && canCombine(storedType, held)) {
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
            return InteractionResult.SUCCESS;
        } else if (!storedType.isEmpty() && ctx.getPlayer() != null) {
            if (!ctx.getLevel().isClientSide) {
                final int count = Math.min(this.storedCount, this.storedType.getMaxStackSize());
                ItemUtil.giveOrDrop(ctx.getPlayer(), ItemHandlerHelper.copyStackWithSize(this.storedType, count));
                consume(count);
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

    public void read(CompoundTag nbt) {
        if (!nbt.contains(COUNT_KEY, Tag.TAG_INT)) {
            var contentStack = ItemStack.of(nbt);
            this.storedCount = contentStack.getCount();
            if (contentStack.isEmpty()) {
                this.storedType = ItemStack.EMPTY;
            } else {
                this.storedType = ItemHandlerHelper.copyStackWithSize(contentStack, 1);
            }
        } else {
            this.storedType = ItemStack.of(nbt.getCompound(ITEM_KEY));
            this.storedCount = nbt.getInt(COUNT_KEY);
        }
    }

    public CompoundTag write() {
        var result = new CompoundTag();
        result.put(ITEM_KEY, storedType.save(new CompoundTag()));
        result.putInt(COUNT_KEY, storedCount);
        return result;
    }

    public String getEmptyKey() {
        return emptyKey;
    }

    public void drop(Consumer<ItemStack> dropper) {
        while (this.storedCount > 0) {
            final int count = Math.min(this.storedCount, this.storedType.getMaxStackSize());
            dropper.accept(ItemHandlerHelper.copyStackWithSize(this.storedType, count));
            consume(count);
        }
        clear();
    }

    public record BigItemStack(ItemStack type, int count) {
        public static final BigItemStack EMPTY = new BigItemStack(ItemStack.EMPTY, 0);
    }
}
