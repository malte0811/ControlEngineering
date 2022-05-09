package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonObject;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.OptionalKeyCopySerializer;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class KeyDataRecipeBuilder extends NoAdvancementShapedBuilder {
    private boolean optional;

    public KeyDataRecipeBuilder(ItemLike pResult, int pCount, boolean optional) {
        super(pResult, pCount, null);
        this.optional = optional;
    }

    public static KeyDataRecipeBuilder shaped(ItemLike result, boolean optional) {
        return shaped(result, 1, optional);
    }

    public static KeyDataRecipeBuilder shaped(RegistryObject<? extends ItemLike> regObject, boolean optional) {
        return shaped(regObject.get(), optional);
    }

    public static KeyDataRecipeBuilder shaped(ItemLike result, int pCount, boolean optional) {
        return new KeyDataRecipeBuilder(result, pCount, optional);
    }

    public static KeyDataRecipeBuilder shaped(
            RegistryObject<? extends ItemLike> regObject, int count, boolean optional
    ) {
        return shaped(regObject.get(), count, optional);
    }

    @Override
    public void save(@Nonnull Consumer<FinishedRecipe> out, @Nonnull ResourceLocation recipeId) {
        super.save(
                recipe -> out.accept(new FinishedRecipe() {
                    @Override
                    public void serializeRecipeData(@Nonnull JsonObject json) {
                        recipe.serializeRecipeData(json);
                        json.addProperty(OptionalKeyCopySerializer.IS_ID_OPTIONAL, optional);
                    }

                    @Nonnull
                    @Override
                    public ResourceLocation getId() {
                        return recipe.getId();
                    }

                    @Nonnull
                    @Override
                    public RecipeSerializer<?> getType() {
                        return CERecipeSerializers.OPTIONAL_KEY_COPY.get();
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
                }),
                recipeId
        );
    }
}
