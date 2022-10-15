package malte0811.controlengineering.crafting.noncrafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.AbstractInt2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.CERecipeTypes;
import malte0811.controlengineering.network.PacketUtils;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ServerFontRecipe extends BaseRecipe {
    public static final ResourceLocation LOCATION = RLUtils.ceLoc("server_font_width");
    public static boolean IN_UNIT_TEST = false;

    private final Int2FloatMap widths;

    public ServerFontRecipe(ResourceLocation id, Int2FloatMap widths) {
        super(id, CERecipeSerializers.FONT_WIDTH, CERecipeTypes.SERVER_FONT.get());
        this.widths = widths;
    }

    public static float getWidth(Level level, String text) {
        if (IN_UNIT_TEST) {
            return text.length();
        }
        var recipe = level.getRecipeManager().byKey(LOCATION).orElse(null);
        if (!(recipe instanceof ServerFontRecipe serverFont)) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) text.chars()
                .mapToDouble(serverFont.widths::get)
                .sum();
    }

    public static class Serializer extends BaseSerializer<ServerFontRecipe> {
        public static final String WIDTHS_KEY = "widths";

        @Nonnull
        @Override
        public ServerFontRecipe fromJson(@Nonnull ResourceLocation recipeId, JsonObject json) {
            var widthsLocal = new Int2FloatOpenHashMap();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject(WIDTHS_KEY).entrySet()) {
                widthsLocal.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsFloat());
            }
            return new ServerFontRecipe(recipeId, widthsLocal);
        }

        @Nullable
        @Override
        public ServerFontRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
            List<Int2FloatMap.Entry> entries = PacketUtils.readList(
                    buffer, fbb -> new AbstractInt2FloatMap.BasicEntry(fbb.readVarInt(), fbb.readFloat())
            );
            Int2FloatMap map = new Int2FloatOpenHashMap(entries.size());
            for (var entry : entries) {
                map.put(entry.getIntKey(), entry.getFloatValue());
            }
            return new ServerFontRecipe(recipeId, map);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull ServerFontRecipe recipe) {
            PacketUtils.writeList(
                    buffer,
                    List.copyOf(recipe.widths.int2FloatEntrySet()),
                    (entry, buf) -> buf.writeVarInt(entry.getIntKey()).writeFloat(entry.getFloatValue())
            );
        }
    }
}
