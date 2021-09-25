package malte0811.controlengineering.datagen;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.font.GlyphProvider;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.ServerFontWidth;
import net.minecraft.client.gui.font.providers.BitmapProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

public class ServerFontData implements DataProvider {
    private final DataGenerator dataGen;
    private final ExistingFileHelper existingFiles;
    private final SimpleReloadableResourceManager clientResources;

    public ServerFontData(DataGenerator dataGen, ExistingFileHelper existingFiles) {
        this.dataGen = dataGen;
        this.existingFiles = existingFiles;
        try {
            Field serverData = ExistingFileHelper.class.getDeclaredField("clientResources");
            serverData.setAccessible(true);
            clientResources = (SimpleReloadableResourceManager) serverData.get(existingFiles);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        final String asciiLoc = "minecraft:font/ascii.png";
        final JsonObject fontDef = new JsonParser().parse(new InputStreamReader(
                clientResources.getResource(new ResourceLocation("font/default.json")).getInputStream()
        )).getAsJsonObject();
        final JsonObject asciiDef = fontDef.getAsJsonArray("providers").get(2).getAsJsonObject();
        Preconditions.checkState(asciiLoc.equals(asciiDef.get("file").getAsString()));
        final BitmapProvider.Builder factory = BitmapProvider.Builder.fromJson(asciiDef);
        final GlyphProvider provider = factory.create(clientResources);
        JsonObject widths = new JsonObject();
        StringBuilder chars = new StringBuilder();
        provider.getSupportedGlyphs()
                .stream()
                .sorted()
                .filter(ch -> ch < 128)
                .forEach(ch -> {
                    widths.addProperty(Integer.toString(ch), provider.getGlyph(ch).getAdvance());
                    chars.append((char) ch.intValue());
                });
        ControlEngineering.LOGGER.info("Supported chars: {}", chars.toString());
        widths.addProperty(Integer.toString(' '), 4);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        DataProvider.save(
                gson,
                cache,
                widths,
                dataGen.getOutputFolder().resolve("data/" + ControlEngineering.MODID + "/" + ServerFontWidth.FILE_NAME)
        );
    }

    @Nonnull
    @Override
    public String getName() {
        return "Server font data";
    }
}
