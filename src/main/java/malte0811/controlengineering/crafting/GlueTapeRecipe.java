package malte0811.controlengineering.crafting;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class GlueTapeRecipe implements ICraftingRecipe {
    private final ResourceLocation id;
    private final Ingredient glue;

    public GlueTapeRecipe(ResourceLocation id, Ingredient glue) {
        this.id = id;
        this.glue = glue;
    }

    @Override
    public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn) {
        return findMatch(inv) >= 0;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
        int match = findMatch(inv);
        if (match < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack tape1 = inv.getStackInSlot(match);
        ItemStack tape2 = inv.getStackInSlot(match + 2);
        byte[] first = PunchedTapeItem.getBytes(tape1);
        byte[] second = PunchedTapeItem.getBytes(tape2);
        byte[] combined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return PunchedTapeItem.withBytes(combined);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 1;
    }

    private int findMatch(CraftingInventory inv) {
        for (int x = 0; x < inv.getWidth() - 2; ++x) {
            for (int y = 0; y < inv.getHeight(); ++y) {
                int offset = y * inv.getWidth() + x;
                ItemStack tape1 = inv.getStackInSlot(offset);
                ItemStack glue = inv.getStackInSlot(offset + 1);
                ItemStack tape2 = inv.getStackInSlot(offset + 2);
                if (tape1.getItem() == CEItems.PUNCHED_TAPE.get() && this.glue.test(glue) &&
                        tape2.getItem() == CEItems.PUNCHED_TAPE.get()) {
                    return offset;
                }
            }
        }
        return -1;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return CEItems.PUNCHED_TAPE.get().getDefaultInstance();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.GLUE_TAPE.get();
    }

    public Ingredient getGlue() {
        return glue;
    }
}
