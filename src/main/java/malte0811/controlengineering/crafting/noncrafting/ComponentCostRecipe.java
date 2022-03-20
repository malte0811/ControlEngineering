package malte0811.controlengineering.crafting.noncrafting;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.JsonObject;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.CERecipeTypes;
import malte0811.controlengineering.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComponentCostRecipe extends BaseRecipe {
    private final List<IngredientWithSize> cost;

    public ComponentCostRecipe(ResourceLocation id, List<IngredientWithSize> cost) {
        super(id, CERecipeSerializers.COMPONENT_COST, CERecipeTypes.COMPONENT_COST);
        this.cost = cost;
    }

    public List<IngredientWithSize> getCost() {
        return cost;
    }

    public static class Serializer extends BaseSerializer<ComponentCostRecipe> {
        public static final String COST_ARRAY_KEY = "costs";

        @Nonnull
        @Override
        public ComponentCostRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            List<IngredientWithSize> totalCost = new ArrayList<>();
            for (var ingredientJSON : json.getAsJsonArray(COST_ARRAY_KEY)) {
                totalCost.add(IngredientWithSize.deserialize(ingredientJSON));
            }
            return new ComponentCostRecipe(recipeId, totalCost);
        }

        @Nullable
        @Override
        public ComponentCostRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
            return new ComponentCostRecipe(recipeId, PacketUtils.readList(buffer, IngredientWithSize::read));
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull ComponentCostRecipe recipe) {
            PacketUtils.writeList(buffer, recipe.getCost(), IngredientWithSize::write);
        }
    }
}
