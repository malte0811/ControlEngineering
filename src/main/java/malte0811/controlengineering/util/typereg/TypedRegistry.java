package malte0811.controlengineering.util.typereg;

import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TypedRegistry<T extends TypedRegistryEntry<?>> {
    private final Map<ResourceLocation, T> entries = new HashMap<>();

    public <T2 extends T> T2 register(ResourceLocation name, T2 instance) {
        Preconditions.checkState(!entries.containsKey(name));
        instance.setRegistryName(name);
        entries.put(name, instance);
        return instance;
    }

    @Nullable
    public T get(ResourceLocation name) {
        return entries.get(name);
    }
}
