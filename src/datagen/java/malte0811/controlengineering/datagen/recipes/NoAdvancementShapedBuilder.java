package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NoAdvancementShapedBuilder extends ShapedRecipeBuilder {
    public NoAdvancementShapedBuilder(ItemLike pResult, int pCount) {
        super(pResult, pCount);
    }

    public static ShapedRecipeBuilder shaped(ItemLike pResult) {
        return shaped(pResult, 1);
    }

    public static ShapedRecipeBuilder shaped(ItemLike pResult, int pCount) {
        return new NoAdvancementShapedBuilder(pResult, pCount);
    }

    @Nonnull
    @Override
    public ShapedRecipeBuilder unlockedBy(
            @Nonnull String pCriterionName, @Nonnull CriterionTriggerInstance pCriterionTrigger
    ) {
        throw new UnsupportedOperationException();
    }

    public void save(@Nonnull Consumer<FinishedRecipe> pFinishedRecipeConsumer, @Nonnull ResourceLocation pRecipeId) {
        super.unlockedBy("dummy", new ImpossibleTrigger.TriggerInstance());
        super.save(
                recipe -> pFinishedRecipeConsumer.accept(new FinishedRecipe() {
                    @Override
                    public void serializeRecipeData(@Nonnull JsonObject pJson) {
                        recipe.serializeRecipeData(pJson);
                    }

                    @Nonnull
                    @Override
                    public ResourceLocation getId() {
                        return recipe.getId();
                    }

                    @Nonnull
                    @Override
                    public RecipeSerializer<?> getType() {
                        return recipe.getType();
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
                }),
                pRecipeId
        );
    }
}
