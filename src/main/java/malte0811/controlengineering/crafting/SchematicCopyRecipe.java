package malte0811.controlengineering.crafting;

import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.ISchematicItem;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record SchematicCopyRecipe(ResourceLocation id) implements CraftingRecipe {
    @Override
    public boolean matches(@Nonnull CraftingContainer container, @Nonnull Level level) {
        return getSchematicToCopy(container) != null;
    }

    @Override
    @Nonnull
    public ItemStack assemble(@Nonnull CraftingContainer container, RegistryAccess access) {
        var schematic = getSchematicToCopy(container);
        if (schematic != null) {
            return ISchematicItem.create(CEItems.SCHEMATIC, schematic.toCopy());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Nonnull
    @Override
    public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingContainer container) {
        NonNullList<ItemStack> remaining = CraftingRecipe.super.getRemainingItems(container);
        var match = getSchematicToCopy(container);
        if (match != null) {
            remaining.set(match.slotCopiedFrom(), container.getItem(match.slotCopiedFrom()).copy());
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    @Nonnull
    public ItemStack getResultItem(RegistryAccess access) {
        return CEItems.SCHEMATIC.get().getDefaultInstance();
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return CERecipeSerializers.SCHEMATIC_COPY.get();
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(CEItems.SCHEMATIC.get()),
                Ingredient.of(CEItems.SCHEMATIC.get(), CEItems.PCB_STACK.get())
        );
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Nullable
    private Match getSchematicToCopy(CraftingContainer container) {
        boolean hasEmpty = false;
        Match matchedSource = null;
        for (int i = 0; i < container.getContainerSize(); ++i) {
            var stackHere = container.getItem(i);
            if (stackHere.getItem() instanceof ISchematicItem) {
                var schematic = ISchematicItem.getSchematic(stackHere);
                if (stackHere.is(CEItems.SCHEMATIC.get()) && Schematic.isEmpty(schematic)) {
                    if (hasEmpty) {
                        return null;
                    }
                    hasEmpty = true;
                } else {
                    if (matchedSource != null || Schematic.isEmpty(schematic)) {
                        return null;
                    }
                    matchedSource = new Match(schematic, i);
                }
            } else if (!stackHere.isEmpty()) {
                return null;
            }
        }
        return hasEmpty ? matchedSource : null;
    }

    private record Match(Schematic toCopy, int slotCopiedFrom) {}
}
