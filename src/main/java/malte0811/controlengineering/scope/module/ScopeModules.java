package malte0811.controlengineering.scope.module;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.util.RLUtils;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class ScopeModules {
    public static final String ITEM_PREFIX = "scope_module_";
    public static final TypedRegistry<ScopeModule<?>> REGISTRY = new TypedRegistry<>();
    public static final ScopeModule<Unit> NONE = register("none", new NoneModule());
    public static final ScopeModule<AnalogModule.State> ANALOG = register("analog", new AnalogModule());
    public static final ScopeModule<DigitalModule.State> DIGITAL = register("digital", new DigitalModule());

    @Nullable
    public static ScopeModule<?> getModule(Item item) {
        final var itemName = BuiltInRegistries.ITEM.getKey(item);
        if (!itemName.getPath().startsWith(ITEM_PREFIX)) { return null; }
        final var modulePath = itemName.getPath().substring(ITEM_PREFIX.length());
        final var moduleName = new ResourceLocation(itemName.getNamespace(), modulePath);
        return REGISTRY.get(moduleName);
    }

    private static <T extends ScopeModule<?>> T register(String path, T module) {
        return REGISTRY.register(RLUtils.ceLoc(path), module);
    }
}
