package malte0811.controlengineering.crafting;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PanelTopItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

//TODO the final recipe should be something different
public class PanelRecipe implements ICraftingRecipe {
    private final ResourceLocation id;

    public PanelRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn) {
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                ItemStack stack = inv.getStackInSlot(x + inv.getWidth() * y);
                if (x == 1 && y == 1) {
                    if (stack.getItem() != CEItems.PANEL_TOP.get() || PanelTopItem.isEmptyPanelTop(stack)) {
                        return false;
                    }
                } else if (!IETags.getTagsFor(EnumMetals.STEEL).plate.contains(stack.getItem())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull CraftingInventory inv) {
        final ItemStack middleStack = inv.getStackInSlot(inv.getWidth() + 1);
        final ItemStack result = getRecipeOutput().copy();
        CompoundNBT resultNBT = middleStack.getTag();
        if (resultNBT == null) {
            resultNBT = new CompoundNBT();
        } else {
            resultNBT = resultNBT.copy();
        }
        //TODO
        new PanelTransform(
                0.25F, (float) Math.toDegrees(Math.atan(0.5)), PanelOrientation.DOWN_NORTH
        ).addTo(resultNBT);
        result.setTag(resultNBT);
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(CEBlocks.CONTROL_PANEL.get());
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.PANEL_RECIPE.get();
    }
}
