package malte0811.controlengineering;

import malte0811.controlengineering.crafting.CERecipeSerializers;
import net.minecraft.data.CustomRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
        CustomRecipeBuilder.customRecipe(CERecipeSerializers.PANEL_RECIPE.get())
                .build(consumer, ControlEngineering.MODID + ":panel");
    }
}
