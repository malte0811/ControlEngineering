package malte0811.controlengineering.datagen;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.data.recipes.RecipeOutput;
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

    public static void buildServerFontData(RecipeOutput out, ExistingFileHelper existingFiles) throws Exception {
        Field serverData = ExistingFileHelper.class.getDeclaredField("clientResources");
        serverData.setAccessible(true);
        final var clientResources = (MultiPackResourceManager) serverData.get(existingFiles);
        final String asciiLoc = "minecraft:font/ascii.png";
        final JsonObject fontDef = JsonParser.parseReader(new InputStreamReader(
                clientResources.getResource(ResourceLocation.withDefaultNamespace("font/include/default.json")).orElseThrow().open()
        )).getAsJsonObject();
        final JsonObject asciiDef = fontDef.getAsJsonArray("providers").get(2).getAsJsonObject();
        Preconditions.checkState(asciiLoc.equals(asciiDef.get("file").getAsString()));
        final var providerDef = GlyphProviderDefinition.MAP_CODEC.codec().parse(JsonOps.INSTANCE, asciiDef)
                .result()
                .orElseThrow();
        final var loader = providerDef.unpack().left().orElseThrow();
        final GlyphProvider provider = loader.load(clientResources);
        var widths = new Int2FloatOpenHashMap();
        Objects.requireNonNull(provider).getSupportedGlyphs()
                .intStream()
                .sorted()
                .filter(ch -> ch < 128)
                .forEach(ch -> widths.put(ch, provider.getGlyph(ch).getAdvance()));
        out.accept(ServerFontRecipe.LOCATION, new ServerFontRecipe(widths), null);
    }
}