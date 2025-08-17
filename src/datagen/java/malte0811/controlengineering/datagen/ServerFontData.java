package malte0811.controlengineering.datagen;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.serialization.JsonOps;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;

public class ServerFontData {

    public static void buildServerFontData(
            @Nonnull Consumer<FinishedRecipe> out, ExistingFileHelper existingFiles
    ) throws Exception {
        Field serverData = ExistingFileHelper.class.getDeclaredField("clientResources");
        serverData.setAccessible(true);
        final var clientResources = (MultiPackResourceManager) serverData.get(existingFiles);
        final String asciiLoc = "minecraft:font/ascii.png";
        final JsonObject fontDef = JsonParser.parseReader(new InputStreamReader(
                clientResources.getResource(ResourceLocation.withDefaultNamespace("font/include/default.json")).orElseThrow().open()
        )).getAsJsonObject();
        final JsonObject asciiDef = fontDef.getAsJsonArray("providers").get(2).getAsJsonObject();
        Preconditions.checkState(asciiLoc.equals(asciiDef.get("file").getAsString()));
        final var providerDef = GlyphProviderDefinition.CODEC.parse(JsonOps.INSTANCE, asciiDef)
                .result()
                .orElseThrow();
        final var loader = providerDef.unpack().left().orElseThrow();
        final GlyphProvider provider = loader.load(clientResources);
        JsonObject widths = new JsonObject();
        Objects.requireNonNull(provider).getSupportedGlyphs()
                .intStream()
                .sorted()
                .filter(ch -> ch < 128)
                .forEach(ch -> widths.addProperty(Integer.toString(ch), provider.getGlyph(ch).getAdvance()));
        out.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(@Nonnull JsonObject json) {
                json.add(ServerFontRecipe.Serializer.WIDTHS_KEY, widths);
            }

            @Nonnull
            @Override
            public ResourceLocation getId() {
                return ServerFontRecipe.LOCATION;
            }

            @Nonnull
            @Override
            public RecipeSerializer<?> getType() {
                return CERecipeSerializers.FONT_WIDTH.get();
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