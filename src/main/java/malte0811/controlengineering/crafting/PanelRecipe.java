package malte0811.controlengineering.crafting;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PanelTopItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;

import javax.annotation.Nonnull;

public record PanelRecipe(
        ResourceLocation id, Ingredient cover
) implements CraftingRecipe, IShapedRecipe<CraftingContainer> {
    @Override
    public boolean matches(@Nonnull CraftingContainer inv, @Nonnull Level worldIn) {
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                ItemStack stack = inv.getItem(x + inv.getWidth() * y);
                if (x == 1 && y == 1) {
                    if (stack.getItem() != CEItems.PANEL_TOP.get() || PanelTopItem.isEmptyPanelTop(stack)) {
                        return false;
                    }
                } else if (!cover.test(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        final ItemStack middleStack = inv.getItem(inv.getWidth() + 1);
        final ItemStack result = getResultItem().copy();
        CompoundTag resultNBT = middleStack.getTag();
        if (resultNBT == null) {
            resultNBT = new CompoundTag();
        } else {
            resultNBT = resultNBT.copy();
        }
        new PanelTransform(
                0.25F, (float) -Math.toDegrees(Math.atan(0.5)), PanelOrientation.DOWN_NORTH
        ).addTo(resultNBT);
        result.setTag(resultNBT);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem() {
        return new ItemStack(CEBlocks.CONTROL_PANEL.get());
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.PANEL_RECIPE.get();
    }

    @Override
    public int getRecipeWidth() {
        return 3;
    }

    @Override
    public int getRecipeHeight() {
        return 3;
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                cover, cover, cover,
                cover, Ingredient.of(CEItems.PANEL_TOP.get()), cover,
                cover, cover, cover
        );
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }
}
