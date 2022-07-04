package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Bus.MOD)
public class CERecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
            Registry.RECIPE_TYPE_REGISTRY, ControlEngineering.MODID
    );

    public static RegistryObject<RecipeType<ComponentCostRecipe>> COMPONENT_COST = register("component_cost");
    public static RegistryObject<RecipeType<ServerFontRecipe>> SERVER_FONT = register("server_font_width");

    private static <T extends Recipe<?>>
    RegistryObject<RecipeType<T>> register(String path) {
        return REGISTER.register(path, () -> new RecipeType<>() {});
    }
}
