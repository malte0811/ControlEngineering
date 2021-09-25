package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlueTapeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<GlueTapeRecipe> {
    public static final String GLUE_KEY = "glue";

    @Nonnull
    @Override
    public GlueTapeRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        Ingredient ingred = Ingredient.fromJson(json.get(GLUE_KEY));
        return new GlueTapeRecipe(recipeId, ingred);
    }

    @Nullable
    @Override
    public GlueTapeRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
        Ingredient ingred = Ingredient.fromNetwork(buffer);
        return new GlueTapeRecipe(recipeId, ingred);
    }

    @Override
    public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull GlueTapeRecipe recipe) {
        recipe.getGlue().toNetwork(buffer);
    }
}
