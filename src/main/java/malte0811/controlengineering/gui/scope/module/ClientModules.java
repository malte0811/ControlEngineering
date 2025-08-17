package malte0811.controlengineering.gui.scope.module;

import malte0811.controlengineering.scope.module.ScopeModule;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClientModules {
    private static final Map<ResourceLocation, ClientModule<?>> MODULE_TEXTURES = new HashMap<>();

    static {
        register(new NoneClientModule());
        register(new AnalogClientModule());
        register(new DigitalClientModule());
    }

    @SuppressWarnings("unchecked")
    public static <T> ClientModule<T> getModule(ScopeModule<T> module) {
        return (ClientModule<T>) Objects.requireNonNull(MODULE_TEXTURES.get(module.getRegistryName()));
    }

    private static <T> void register(ClientModule<T> module) {
        MODULE_TEXTURES.put(module.getServerModule().getRegistryName(), module);
    }
}
