package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.GlueTapeSerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class GlueTapeBuilder {
    private final Ingredient glue;

    public GlueTapeBuilder(Ingredient glue) {
        this.glue = glue;
    }

    public static GlueTapeBuilder customRecipe(Ingredient glue) {
        return new GlueTapeBuilder(glue);
    }

    public void build(Consumer<FinishedRecipe> consumerIn, final String id) {
        consumerIn.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(@Nonnull JsonObject json) {
                json.add(GlueTapeSerializer.GLUE_KEY, glue.toJson());
            }

            @Nonnull
            @Override
            public ResourceLocation getId() {
                return new ResourceLocation(ControlEngineering.MODID, id);
            }

            @Nonnull
            @Override
            public RecipeSerializer<?> getType() {
                return CERecipeSerializers.GLUE_TAPE.get();
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return null;
            }
        });
    }
}
