package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IEItemRefs {
    public static final ItemLike CIRCUIT_BOARD = of("circuit_board");
    public static final ItemLike TUBE = of("electron_tube");
    public static final ItemLike WIRE = of("wire_copper");
    public static final ItemLike COPPER_WIRE_COIL = of("wirecoil_copper");
    public static final ItemLike REDSTONE_WIRE_COIL = of("wirecoil_redstone");
    public static final ItemLike REDSTONE_CONNECTOR = of("connector_redstone");

    // Classload early
    public static void init() {}

    private static ItemLike of(String path) {
        var regObject = RegistryObject.of(new ResourceLocation(Lib.MODID, path), ForgeRegistries.ITEMS);
        return regObject::get;
    }
}