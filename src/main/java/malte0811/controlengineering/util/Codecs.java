package malte0811.controlengineering.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;

public class Codecs {
    public static final Codec<INBT> INBT_CODEC = Codec.PASSTHROUGH.flatXmap(
            dyn -> DataResult.success(dyn.convert(NBTDynamicOps.INSTANCE).getValue()),
            inbt -> DataResult.success(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt))
    );

    public static final Codec<Direction> DIRECTION_CODEC = Codec.INT.xmap(i -> Direction.VALUES[i], Direction::ordinal);

    public static <T> T read(Codec<T> codec, CompoundNBT in, String subName) {
        return read(codec, in.get(subName));
    }

    public static <T> T read(Codec<T> codec, INBT nbt) {
        return codec.decode(NBTDynamicOps.INSTANCE, nbt)
                .getOrThrow(false, s -> {})
                .getFirst();
    }

    public static <T> void add(Codec<T> codec, T value, CompoundNBT out, String subName) {
        out.put(subName, encode(codec, value));
    }

    public static <T> INBT encode(Codec<T> codec, T value) {
        return codec.encodeStart(NBTDynamicOps.INSTANCE, value).getOrThrow(false, s -> {});
    }

}
