package malte0811.controlengineering.crafting;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public record SimpleRecipeSerializer<R extends Recipe<?>>(DualMapCodec<RegistryFriendlyByteBuf, R> codecs) implements RecipeSerializer<R> {
    public static <R extends Recipe<?>> RecipeSerializer<R> unit(R makeNew) {
        return new SimpleRecipeSerializer<>(DualMapCodec.unit(makeNew));
    }

    @Override
    public MapCodec<R> codec() {
        return codecs.mapCodec();
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return codecs.streamCodec();
    }
}
