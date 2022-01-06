package malte0811.controlengineering.gui.misc;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record SyncContainer(Consumer<ItemStack> setStack, Supplier<ItemStack> getStack) implements Container {

    public static Slot makeSyncSlot(int id, Consumer<ItemStack> setStack, Supplier<ItemStack> getStack) {
        return new Slot(new SyncContainer(setStack, getStack), id, 0, 0);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getStack.get().isEmpty();
    }

    @Nonnull
    @Override
    public ItemStack getItem(int pIndex) {
        return getStack.get();
    }

    @Nonnull
    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        return getStack.get().split(pCount);
    }

    @Nonnull
    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        var oldStack = getStack.get();
        setStack.accept(ItemStack.EMPTY);
        return oldStack;
    }

    @Override
    public void setItem(int pIndex, @Nonnull ItemStack pStack) {
        setStack.accept(pStack);
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(@Nonnull Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {
        setStack.accept(ItemStack.EMPTY);
    }
}
