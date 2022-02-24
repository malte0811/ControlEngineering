package malte0811.controlengineering.crafting;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PunchedTapeItem;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

import javax.annotation.Nonnull;
import java.util.Arrays;

public record GlueTapeRecipe(ResourceLocation id,
                             Ingredient glue) implements CraftingRecipe, IShapedRecipe<CraftingContainer> {

    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level worldIn) {
        return findMatch(inv) >= 0;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        int match = findMatch(inv);
        if (match < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack tape1 = inv.getItem(match);
        ItemStack tape2 = inv.getItem(match + 2);
        byte[] first = PunchedTapeItem.getBytes(tape1);
        byte[] second = PunchedTapeItem.getBytes(tape2);
        byte[] combined = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, combined, first.length, second.length);
        return PunchedTapeItem.withBytes(combined);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    private int findMatch(CraftingContainer inv) {
        for (int x = 0; x < inv.getWidth() - 2; ++x) {
            for (int y = 0; y < inv.getHeight(); ++y) {
                int offset = y * inv.getWidth() + x;
                ItemStack tape1 = inv.getItem(offset);
                ItemStack glue = inv.getItem(offset + 1);
                ItemStack tape2 = inv.getItem(offset + 2);
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
    public ItemStack getResultItem() {
        return CEItems.PUNCHED_TAPE.get().getDefaultInstance();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(CEItems.PUNCHED_TAPE.get()),
                glue,
                Ingredient.of(CEItems.PUNCHED_TAPE.get())
        );
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.GLUE_TAPE.get();
    }

    @Override
    public int getRecipeWidth() {
        return 3;
    }

    @Override
    public int getRecipeHeight() {
        return 1;
    }
}
