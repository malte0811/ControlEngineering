package malte0811.controlengineering.crafting;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.noncrafting.ComponentCostRecipe;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CERecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTER = DeferredRegister.create(
            Registries.RECIPE_SERIALIZER, ControlEngineering.MODID
    );

    public static final Supplier<RecipeSerializer<PanelRecipe>> PANEL_RECIPE = REGISTER.register(
            "panel", createSerializer(PanelRecipe.CODECS)
    );
    public static final Supplier<RecipeSerializer<GlueTapeRecipe>> GLUE_TAPE = REGISTER.register(
            "glue_tape", createSerializer(GlueTapeRecipe.CODECS)
    );
    public static final Supplier<RecipeSerializer<ComponentCostRecipe>> COMPONENT_COST = REGISTER.register(
            "component_cost", createSerializer(ComponentCostRecipe.CODECS)
    );
    public static final Supplier<RecipeSerializer<ServerFontRecipe>> FONT_WIDTH = REGISTER.register(
            "server_font_width", createSerializer(ServerFontRecipe.CODECS)
    );
    public static final Supplier<RecipeSerializer<SchematicCopyRecipe>> SCHEMATIC_COPY = REGISTER.register(
            "schematic_copy", () -> SimpleRecipeSerializer.unit(new SchematicCopyRecipe())
    );
    public static final Supplier<RecipeSerializer<OptionalKeyCopyRecipe>> OPTIONAL_KEY_COPY = REGISTER.register(
            "key_copy", createSerializer(OptionalKeyCopyRecipe.CODECS)
    );

    private static <R extends Recipe<?>> Supplier<RecipeSerializer<R>> createSerializer(DualMapCodec<RegistryFriendlyByteBuf, R> codec) {
        return () -> new SimpleRecipeSerializer<>(codec);
    }
}
