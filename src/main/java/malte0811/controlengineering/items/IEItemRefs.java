package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

public class IEItemRefs {
    public static final RegistryObject<Item> CIRCUIT_BOARD = of("circuit_board");
    public static final RegistryObject<Item> TUBE = of("electron_tube");
    public static final RegistryObject<Item> LV_WIRE_COIL = of("wirecoil_copper");
    public static final RegistryObject<Item> WIRE = of("wire_copper");

    // Classload early
    public static void init() {}

    private static RegistryObject<Item> of(String path) {
        return RegistryObject.of(new ResourceLocation(Lib.MODID, path), ForgeRegistries.ITEMS);
    }
}
