package malte0811.controlengineering.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class ServerFontWidth {
    public static final String FILE_NAME = "char_widths.json";
    private static Int2FloatMap widths = null;

    private static Int2FloatMap getWidths() {
        if (widths == null) {
            try {
                Resource resource = ServerLifecycleHooks.getCurrentServer()
                        .getResourceManager()
                        .getResource(new ResourceLocation(ControlEngineering.MODID, FILE_NAME));
                JsonElement json = new JsonParser().parse(new InputStreamReader(resource.getInputStream()));
                widths = new Int2FloatOpenHashMap();
                for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                    widths.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsFloat());
                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        return widths;
    }

    public static void constantWidthForTesting(float width) {
        widths = new Int2FloatOpenHashMap();
        for (int i = 0; i < 128; ++i) {
            widths.put(i, width);
        }
    }

    public static float getWidth(String s) {
        return (float) s.chars()
                .mapToDouble(i -> getWidths().get(i))
                .sum();
    }
}
