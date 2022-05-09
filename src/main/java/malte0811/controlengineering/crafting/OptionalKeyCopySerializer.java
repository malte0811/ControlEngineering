package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OptionalKeyCopySerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<OptionalKeyCopyRecipe> {
    public static final String IS_ID_OPTIONAL = "isIdOptional";

    @Nonnull
    @Override
    public OptionalKeyCopyRecipe fromJson(
            @Nonnull ResourceLocation recipeId, @Nonnull JsonObject serializedRecipe
    ) {
        final var baseRecipe = SHAPED_RECIPE.fromJson(recipeId, serializedRecipe);
        final var isOptional = serializedRecipe.get(IS_ID_OPTIONAL).getAsBoolean();
        return new OptionalKeyCopyRecipe(baseRecipe, isOptional);
    }

    @Override
    public OptionalKeyCopyRecipe fromJson(
            ResourceLocation recipeLoc, JsonObject recipeJson, ICondition.IContext context
    ) {
        final var baseRecipe = SHAPED_RECIPE.fromJson(recipeLoc, recipeJson, context);
        final var isOptional = recipeJson.get(IS_ID_OPTIONAL).getAsBoolean();
        return new OptionalKeyCopyRecipe(baseRecipe, isOptional);
    }

    @Nullable
    @Override
    public OptionalKeyCopyRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        final var baseRecipe = SHAPED_RECIPE.fromNetwork(recipeId, buffer);
        final var isOptional = buffer.readBoolean();
        if (baseRecipe != null) {
            return new OptionalKeyCopyRecipe(baseRecipe, isOptional);
        } else {
            return null;
        }
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull OptionalKeyCopyRecipe recipe) {
        SHAPED_RECIPE.toNetwork(buffer, recipe);
        buffer.writeBoolean(recipe.isIdOptional());
    }
}
