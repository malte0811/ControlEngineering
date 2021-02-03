package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.DataResult;
import malte0811.controlengineering.util.serialization.StringSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public final class PanelComponentType<T extends PanelComponent<T>> {
    private final ResourceLocation name;
    private final StringSerializer<T> codec;
    private final Supplier<T> createEmpty;

    public PanelComponentType(ResourceLocation name, StringSerializer<T> codec, Supplier<T> createEmpty) {
        this.name = name;
        this.codec = codec;
        this.createEmpty = createEmpty;
    }

    public CompoundNBT toNBT(T instance) {
        return codec.toNBT(instance);
    }

    public DataResult<T> fromNBT(CompoundNBT data) {
        return codec.fromNBT(data).map(r -> {
            r.setType(this);
            return r;
        });
    }

    public T empty() {
        T empty = createEmpty.get();
        empty.setType(this);
        return empty;
    }

    public StringSerializer<T> getCodec() {
        return codec;
    }

    public ResourceLocation getName() {
        return name;
    }

    public DataResult<T> fromString(List<String> subList) {
        return getCodec().fromString(subList).map(c -> {
            c.setType(this);
            return c;
        });
    }
}
