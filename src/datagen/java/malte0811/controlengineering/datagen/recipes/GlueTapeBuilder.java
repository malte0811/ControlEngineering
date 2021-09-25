package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonObject;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.GlueTapeSerializer;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

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

    public void build(Consumer<IFinishedRecipe> consumerIn, final String id) {
        consumerIn.accept(new IFinishedRecipe() {
            @Override
            public void serialize(@Nonnull JsonObject json) {
                json.add(GlueTapeSerializer.GLUE_KEY, glue.serialize());
            }

            @Nonnull
            @Override
            public ResourceLocation getID() {
                return new ResourceLocation(ControlEngineering.MODID, id);
            }

            @Nonnull
            @Override
            public IRecipeSerializer<?> getSerializer() {
                return CERecipeSerializers.GLUE_TAPE.get();
            }

            @Nullable
            @Override
            public JsonObject getAdvancementJson() {
                return null;
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementID() {
                return null;
            }
        });
    }
}
