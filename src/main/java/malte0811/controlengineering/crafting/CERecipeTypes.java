package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Bus.MOD)
public class CERecipeTypes {
    public static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(
            Registries.RECIPE_TYPE, ControlEngineering.MODID
    );

    public static RegistryObject<RecipeType<ComponentCostRecipe>> COMPONENT_COST = REGISTER.register("component_cost",
            () -> new RecipeType<ComponentCostRecipe>() { });
    public static RegistryObject<RecipeType<ServerFontRecipe>> SERVER_FONT = REGISTER.register("server_font_width",
            () -> new RecipeType<ServerFontRecipe>() { });

//    private static <T extends Recipe<?>>
//    RegistryObject<RecipeType<T>> register(String path) {
//        return REGISTER.register(path, () -> new RecipeType<>() {});
//    }
}
