package malte0811.controlengineering.crafting;

import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.itemdata.CEDataComponents;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PanelTopItem;
import malte0811.controlengineering.util.CEDualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public record PanelRecipe(Ingredient cover) implements CraftingRecipe {
    public static final DualMapCodec<RegistryFriendlyByteBuf, PanelRecipe> CODECS = CEDualCodecs.INGREDIENT
            .map(PanelRecipe::new, PanelRecipe::cover)
            .fieldOf("cover");

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level worldIn) {
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                ItemStack stack = inv.getItem(x + inv.width() * y);
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
    public ItemStack assemble(@Nonnull CraftingInput inv, HolderLookup.Provider access) {
        final ItemStack middleStack = inv.getItem(inv.width() + 1);
        final ItemStack result = getResultItem(access).copy();
        result.copyFrom(middleStack, CEDataComponents.PANEL_COMPONENTS.get());
        result.set(CEDataComponents.PANEL_TRANSFORM, new PanelTransform.BETransformData());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        return new ItemStack(CEBlocks.CONTROL_PANEL.get());
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.PANEL_RECIPE.get();
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
