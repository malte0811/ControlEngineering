package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class CERecipeSerializers {
    public static final DeferredRegister<IRecipeSerializer<?>> REGISTER = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ControlEngineering.MODID
    );

    public static final RegistryObject<SpecialRecipeSerializer<PanelRecipe>> PANEL_RECIPE = REGISTER.register(
            "panel", () -> new SpecialRecipeSerializer<>(PanelRecipe::new)
    );
    public static final RegistryObject<GlueTapeSerializer> GLUE_TAPE = REGISTER.register(
            "glue_tape", GlueTapeSerializer::new
    );
}
