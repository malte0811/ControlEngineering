package malte0811.controlengineering.util;

import blusunrize.immersiveengineering.api.Lib;
import malte0811.controlengineering.ControlEngineering;
import net.minecraft.resources.ResourceLocation;

public class RLUtils {
    public static ResourceLocation ceLoc(String path) {
        return new ResourceLocation(ControlEngineering.MODID, path);
    }

    public static ResourceLocation ieLoc(String path) {
        return new ResourceLocation(Lib.MODID, path);
    }
}
