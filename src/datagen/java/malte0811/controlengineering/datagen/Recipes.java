package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.datagen.recipes.GlueTapeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        SpecialRecipeBuilder.special(CERecipeSerializers.PANEL_RECIPE.get())
                .save(consumer, ControlEngineering.MODID + ":panel");
        GlueTapeBuilder.customRecipe(Ingredient.of(Tags.Items.SLIMEBALLS))
                .build(consumer, "glue_tape");
    }
}
