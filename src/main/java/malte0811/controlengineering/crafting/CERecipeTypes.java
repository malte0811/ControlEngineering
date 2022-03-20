package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Bus.MOD)
public class CERecipeTypes {
    public static RecipeType<ComponentCostRecipe> COMPONENT_COST;

    @SubscribeEvent
    // Just need *some* registry event, since all registries are apparently unfrozen during those
    public static void register(RegistryEvent.Register<Block> ev) {
        COMPONENT_COST = register("component_cost");
    }

    private static <T extends Recipe<?>> RecipeType<T> register(String path) {
        var name = new ResourceLocation(ControlEngineering.MODID, path);
        return Registry.register(Registry.RECIPE_TYPE, name, new RecipeType<T>() {});
    }
}
