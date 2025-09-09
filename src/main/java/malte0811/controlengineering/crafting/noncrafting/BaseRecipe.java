package malte0811.controlengineering.crafting.noncrafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BaseRecipe implements Recipe<SingleRecipeInput> {
    private final RecipeSerializer<?> serializer;
    private final RecipeType<?> type;

    public BaseRecipe(Supplier<? extends RecipeSerializer<?>> serializer, RecipeType<?> type) {
        this.serializer = serializer.get();
        this.type = type;
    }

    @Override
    public boolean matches(@Nonnull SingleRecipeInput pContainer, @Nonnull Level pLevel) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull SingleRecipeInput pContainer, HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getResultItem(HolderLookup.Provider access) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Nonnull
    @Override
    public RecipeType<?> getType() {
        return type;
    }

    // TODO remove now that ForgeRegEntry is gone?
    protected static abstract class BaseSerializer<R extends BaseRecipe> implements RecipeSerializer<R> { }
}
