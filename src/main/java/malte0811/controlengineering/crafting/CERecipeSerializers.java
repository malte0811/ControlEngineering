package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ControlEngineering.MODID
    );

    public static final RegistryObject<SimpleRecipeSerializer<PanelRecipe>> PANEL_RECIPE = REGISTER.register(
            "panel", () -> new SimpleRecipeSerializer<>(PanelRecipe::new)
    );
    public static final RegistryObject<GlueTapeSerializer> GLUE_TAPE = REGISTER.register(
            "glue_tape", GlueTapeSerializer::new
    );
}
