package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CERecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
            Registries.RECIPE_TYPE, ControlEngineering.MODID
    );

    public static DeferredHolder<RecipeType<?>, RecipeType<ComponentCostRecipe>> COMPONENT_COST = register("component_cost");
    public static DeferredHolder<RecipeType<?>, RecipeType<ServerFontRecipe>> SERVER_FONT = register("server_font_width");

    private static <T extends Recipe<?>>
    DeferredHolder<RecipeType<?>, RecipeType<T>> register(String path) {
        return REGISTER.register(path, () -> new RecipeType<>() {});
    }
}
