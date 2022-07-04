package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SingleIngredientRecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R> {
    private final String key;
    private final BiFunction<ResourceLocation, Ingredient, R> makeRecipe;
    private final Function<R, Ingredient> getIngredient;

    public SingleIngredientRecipeSerializer(
            String key, BiFunction<ResourceLocation, Ingredient, R> makeRecipe, Function<R, Ingredient> getIngredient
    ) {
        this.key = key;
        this.makeRecipe = makeRecipe;
        this.getIngredient = getIngredient;
    }

    @Nonnull
    @Override
    public R fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        Ingredient ingred = Ingredient.fromJson(json.get(getKey()));
        return makeRecipe.apply(recipeId, ingred);
    }

    @Nullable
    @Override
    public R fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        Ingredient ingred = Ingredient.fromNetwork(buffer);
        return makeRecipe.apply(recipeId, ingred);
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull R recipe) {
        getIngredient.apply(recipe).toNetwork(buffer);
    }

    public String getKey() {
        return key;
    }
}
