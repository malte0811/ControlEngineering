package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.ForgeRegistries;

public class IEItemRefs {
    public static final Lazy<Item> CIRCUIT_BOARD = of("circuit_board");

    private static Lazy<Item> of(String path) {
        return Lazy.of(() -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(Lib.MODID, path)));
    }
}
