package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlueTapeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
        implements IRecipeSerializer<GlueTapeRecipe> {
    public static final String GLUE_KEY = "glue";

    @Nonnull
    @Override
    public GlueTapeRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
        Ingredient ingred = Ingredient.deserialize(json.get(GLUE_KEY));
        return new GlueTapeRecipe(recipeId, ingred);
    }

    @Nullable
    @Override
    public GlueTapeRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer) {
        Ingredient ingred = Ingredient.read(buffer);
        return new GlueTapeRecipe(recipeId, ingred);
    }

    @Override
    public void write(@Nonnull PacketBuffer buffer, @Nonnull GlueTapeRecipe recipe) {
        recipe.getGlue().write(buffer);
    }
}
