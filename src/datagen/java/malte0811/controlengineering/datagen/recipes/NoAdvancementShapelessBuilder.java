package malte0811.controlengineering.datagen.recipes;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NoAdvancementShapelessBuilder extends ShapelessRecipeBuilder {
    @Nullable
    private final CompoundTag nbt;

    public NoAdvancementShapelessBuilder(ItemLike pResult, int pCount, @Nullable CompoundTag nbt) {
        super(RecipeCategory.MISC, pResult, pCount);
        this.nbt = nbt;
    }

    public static ShapelessRecipeBuilder shapeless(ItemStack result) {
        return new NoAdvancementShapelessBuilder(result.getItem(), result.getCount(), result.getTag());
    }

    public static ShapelessRecipeBuilder shapeless(ItemLike result) {
        return shapeless(result, 1);
    }

    public static ShapelessRecipeBuilder shapeless(RegistryObject<? extends ItemLike> regObject) {
        return shapeless(regObject.get());
    }

    public static ShapelessRecipeBuilder shapeless(ItemLike result, int pCount) {
        return new NoAdvancementShapelessBuilder(result, pCount, null);
    }

    @Nonnull
    @Override
    public ShapelessRecipeBuilder unlockedBy(
            @Nonnull String pCriterionName, @Nonnull CriterionTriggerInstance pCriterionTrigger
    ) {
        throw new UnsupportedOperationException();
    }

    public void save(@Nonnull Consumer<FinishedRecipe> out, @Nonnull ResourceLocation recipeId) {
        super.unlockedBy("dummy", new ImpossibleTrigger.TriggerInstance());
        super.save(recipe -> out.accept(new NoAdvancementFinishedRecipe(recipe, nbt)), recipeId);
    }
}
