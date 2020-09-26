package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PairCodec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class PanelComponentType<T extends PanelComponent<T>> {
    private final ResourceLocation name;
    private final Codec<T> codec;
    private final Supplier<T> createEmpty;

    public PanelComponentType(ResourceLocation name, Codec<T> codec, Supplier<T> createEmpty) {
        this.name = name;
        this.codec = codec;
        this.createEmpty = createEmpty;
    }

    public INBT toNBT(T instance) {
        return codec.encodeStart(NBTDynamicOps.INSTANCE, instance)
                .getOrThrow(false, s -> {});
    }

    public T fromNBT(INBT data) {
        T result = codec.decode(NBTDynamicOps.INSTANCE, data)
                .getOrThrow(false, s -> {})
                .getFirst();
        result.setType(this);
        return result;
    }

    public T empty() {
        T empty = createEmpty.get();
        empty.setType(this);
        return empty;
    }

    public Codec<T> getCodec() {
        return codec;
    }

    public ResourceLocation getName() {
        return name;
    }
}
