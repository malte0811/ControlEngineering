package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(
            Registries.RECIPE_SERIALIZER, ControlEngineering.MODID
    );

    public static final Supplier<SingleIngredientRecipeSerializer<?>> PANEL_RECIPE = REGISTER.register(
            "panel", () -> new SingleIngredientRecipeSerializer<>("cover", PanelRecipe::new, PanelRecipe::cover)
    );
    public static final Supplier<SingleIngredientRecipeSerializer<?>> GLUE_TAPE = REGISTER.register(
            "glue_tape", () -> new SingleIngredientRecipeSerializer<>("glue", GlueTapeRecipe::new, GlueTapeRecipe::glue)
    );
    public static final Supplier<ComponentCostRecipe.Serializer> COMPONENT_COST = REGISTER.register(
            "component_cost", ComponentCostRecipe.Serializer::new
    );
    public static final Supplier<ServerFontRecipe.Serializer> FONT_WIDTH = REGISTER.register(
            "server_font_width", ServerFontRecipe.Serializer::new
    );
    public static final Supplier<SimpleRecipeSerializer<SchematicCopyRecipe>> SCHEMATIC_COPY = REGISTER.register(
            "schematic_copy", () -> new SimpleRecipeSerializer<>(SchematicCopyRecipe::new)
    );
    public static final Supplier<OptionalKeyCopySerializer> OPTIONAL_KEY_COPY = REGISTER.register(
            "key_copy", OptionalKeyCopySerializer::new
    );
}
