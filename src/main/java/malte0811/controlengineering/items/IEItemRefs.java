package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class IEItemRefs {
    public static final ItemLike CIRCUIT_BOARD = of("circuit_board");
    public static final ItemLike TUBE = of("electron_tube");
    public static final ItemLike WIRE = of("wire_copper");
    public static final ItemLike COPPER_WIRE_COIL = of("wirecoil_copper");
    public static final ItemLike REDSTONE_WIRE_COIL = of("wirecoil_redstone");
    public static final ItemLike REDSTONE_CONNECTOR = of("connector_redstone");
    public static final ItemLike DRILL_HEAD_IRON = of("drillhead_iron");
    public static final ItemLike LOGIC_CIRCUIT = of("logic_circuit");
    public static final ItemLike BLUEPRINT = of("blueprint");
    public static final ItemLike COMPONENT_IRON = of("component_iron");
    public static final RegistryObject<Block> LOGIC_UNIT = of("logic_unit", ForgeRegistries.BLOCKS);
    public static final RegistryObject<Block> RADIATOR = of("radiator", ForgeRegistries.BLOCKS);
    public static final RegistryObject<Block> CRATE = of("crate", ForgeRegistries.BLOCKS);
    public static final RegistryObject<Block> LIGHT_ENGINEERING = of("light_engineering", ForgeRegistries.BLOCKS);

    // Classload early
    public static void init() {}

    private static ItemLike of(String path) {
        var regObject = of(path, ForgeRegistries.ITEMS);
        return regObject::get;
    }

    private static <T> RegistryObject<T> of(String name, IForgeRegistry<T> registry) {
        return RegistryObject.create(new ResourceLocation(Lib.MODID, name), registry);
    }
}
