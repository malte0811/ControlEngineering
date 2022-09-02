package malte0811.controlengineering.util.typereg;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypedRegistry<T extends TypedRegistryEntry<?, ?>> {
    private final Map<ResourceLocation, T> entries = new LinkedHashMap<>();

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

    public T getOrDefault(ResourceLocation name, T fallback) {
        return entries.getOrDefault(name, fallback);
    }

    public Map<ResourceLocation, T> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    public Collection<T> getValues() {
        return entries.values();
    }
}
