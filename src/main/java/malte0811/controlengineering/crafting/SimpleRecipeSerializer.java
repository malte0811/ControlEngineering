package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record SimpleRecipeSerializer<R extends Recipe<?>>(
        Function<ResourceLocation, R> create
) implements RecipeSerializer<R> {
    @Override
    public @NotNull R fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
        return create.apply(recipeId);
    }

    @Override
    public @Nullable R fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        return create.apply(recipeId);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull R recipe) {
    }
}
