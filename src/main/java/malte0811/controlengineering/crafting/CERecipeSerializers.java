package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
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
    public static final RegistryObject<ServerFontRecipe.Serializer> FONT_WIDTH = REGISTER.register(
            "server_font_width", ServerFontRecipe.Serializer::new
    );
    public static final RegistryObject<SimpleRecipeSerializer<SchematicCopyRecipe>> SCHEMATIC_COPY = REGISTER.register(
            "schematic_copy", () -> new SimpleRecipeSerializer<>(SchematicCopyRecipe::new)
    );
    public static final RegistryObject<OptionalKeyCopySerializer> OPTIONAL_KEY_COPY = REGISTER.register(
            "key_copy", OptionalKeyCopySerializer::new
    );
}
