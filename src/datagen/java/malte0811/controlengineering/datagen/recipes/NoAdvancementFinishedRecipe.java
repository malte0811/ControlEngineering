package malte0811.controlengineering.datagen.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record NoAdvancementFinishedRecipe(
        FinishedRecipe original, @Nullable CompoundTag nbt
) implements FinishedRecipe {
    @Override
    public void serializeRecipeData(@Nonnull JsonObject out) {
        original.serializeRecipeData(out);
        if (nbt != null) {
            JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbt);
            out.getAsJsonObject("result").add("nbt", json);
        }
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return original.getId();
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getType() {
        return original.getType();
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
}
