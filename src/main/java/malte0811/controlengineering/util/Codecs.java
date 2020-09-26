package malte0811.controlengineering.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;

public class Codecs {
    public static final Codec<INBT> INBT_CODEC = Codec.PASSTHROUGH.flatXmap(
            dyn -> DataResult.success(dyn.convert(NBTDynamicOps.INSTANCE).getValue()),
            inbt -> DataResult.success(new Dynamic<>(NBTDynamicOps.INSTANCE, inbt))
    );
}
