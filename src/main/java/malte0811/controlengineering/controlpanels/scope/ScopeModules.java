package malte0811.controlengineering.controlpanels.scope;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.resources.ResourceLocation;

public class ScopeModules {
    public static final TypedRegistry<ScopeModule<?>> REGISTRY = new TypedRegistry<>();
    public static final ScopeModule<?> NONE = register("none", new NoneModule());

    private static <T extends ScopeModule<?>> T register(String path, T module) {
        return REGISTRY.register(new ResourceLocation(ControlEngineering.MODID, path), module);
    }
}
