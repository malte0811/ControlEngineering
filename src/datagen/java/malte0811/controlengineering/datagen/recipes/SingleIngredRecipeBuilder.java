package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.SingleIngredientRecipeSerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SingleIngredRecipeBuilder {
    private final SingleIngredientRecipeSerializer<?> serializer;
    private Ingredient onlyIngredient;

    private SingleIngredRecipeBuilder(SingleIngredientRecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static SingleIngredRecipeBuilder special(Supplier<SingleIngredientRecipeSerializer<?>> serializer) {
        return new SingleIngredRecipeBuilder(serializer.get());
    }

    public SingleIngredRecipeBuilder input(Ingredient input) {
        this.onlyIngredient = input;
        return this;
    }

    public void save(Consumer<FinishedRecipe> consumerIn, final String id) {
        consumerIn.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(@Nonnull JsonObject json) {
                json.add(serializer.getKey(), onlyIngredient.toJson());
            }

            @Nonnull
            @Override
            public ResourceLocation getId() {
                return ControlEngineering.ceLoc(id);
            }

            @Nonnull
            @Override
            public RecipeSerializer<?> getType() {
                return serializer;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
