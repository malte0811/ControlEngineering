package malte0811.controlengineering.util;

import malte0811.dualcodecs.DualCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;

public class CEDualCodecs {
    public static final DualCodec<RegistryFriendlyByteBuf, Ingredient> INGREDIENT = new DualCodec<>(
            Ingredient.CODEC, Ingredient.CONTENTS_STREAM_CODEC
    );
}
