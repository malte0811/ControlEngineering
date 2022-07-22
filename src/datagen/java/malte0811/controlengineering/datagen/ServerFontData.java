package malte0811.controlengineering.datagen;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Consumer;

public class ServerFontData extends RecipeProvider {
    private final MultiPackResourceManager clientResources;

    public ServerFontData(DataGenerator dataGen, ExistingFileHelper existingFiles) {
        super(dataGen);
        try {
            Field serverData = ExistingFileHelper.class.getDeclaredField("clientResources");
            serverData.setAccessible(true);
            clientResources = (MultiPackResourceManager) serverData.get(existingFiles);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> out) {
        try {
            buildCraftingRecipesInner(out);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    private void buildCraftingRecipesInner(@Nonnull Consumer<FinishedRecipe> out) throws IOException {
        final String asciiLoc = "minecraft:font/ascii.png";
        final JsonObject fontDef = JsonParser.parseReader(new InputStreamReader(
                clientResources.getResource(new ResourceLocation("font/default.json")).orElseThrow().open()
        )).getAsJsonObject();
        final JsonObject asciiDef = fontDef.getAsJsonArray("providers").get(3).getAsJsonObject();
        Preconditions.checkState(asciiLoc.equals(asciiDef.get("file").getAsString()));
        final BitmapProvider.Builder factory = BitmapProvider.Builder.fromJson(asciiDef);
        final GlyphProvider provider = factory.create(clientResources);
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

    @Nonnull
    @Override
    public String getName() {
        return "Server font data";
    }
}
