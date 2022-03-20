package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ControlEngineering.MODID
    );

    public static final RegistryObject<SingleIngredientRecipeSerializer<?>> PANEL_RECIPE = REGISTER.register(
            "panel", () -> new SingleIngredientRecipeSerializer<>("cover", PanelRecipe::new, PanelRecipe::cover)
    );
    public static final RegistryObject<SingleIngredientRecipeSerializer<?>> GLUE_TAPE = REGISTER.register(
            "glue_tape", () -> new SingleIngredientRecipeSerializer<>("glue", GlueTapeRecipe::new, GlueTapeRecipe::glue)
    );
    public static final RegistryObject<ComponentCostRecipe.Serializer> COMPONENT_COST = REGISTER.register(
            "component_cost", ComponentCostRecipe.Serializer::new
    );
}
