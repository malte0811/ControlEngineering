package malte0811.controlengineering.items;

import malte0811.controlengineering.util.RLUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

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
    public static final ItemLike COMPONENT_ADVANCED = of("component_electronic_adv");
    public static final ItemLike COMPONENT_BASIC = of("component_electronic");
    public static final Supplier<Block> LOGIC_UNIT = of("logic_unit", Registries.BLOCK);
    public static final Supplier<Block> RADIATOR = of("radiator", Registries.BLOCK);
    public static final Supplier<Block> CRATE = of("crate", Registries.BLOCK);
    public static final Supplier<Block> LIGHT_ENGINEERING = of("light_engineering", Registries.BLOCK);
    public static final Supplier<Block> LIGHT_BLUE_SHEETMETAL = of(
            "sheetmetal_colored_" + DyeColor.LIGHT_BLUE.getName(), Registries.BLOCK
    );

    // Classload early
    public static void init() { }

    private static ItemLike of(String path) {
        var regObject = of(path, Registries.ITEM);
        return regObject::get;
    }

    private static <T> Supplier<T> of(String name, ResourceKey<Registry<T>> registry) {
        return DeferredHolder.create(registry, RLUtils.ieLoc(name));
    }
}
