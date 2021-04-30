package malte0811.controlengineering.items;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class CEItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            ControlEngineering.MODID
    );

    //Items
    public static final RegistryObject<BusCoilItem> BUS_WIRE_COIL = REGISTER.register(
            "bus_wire_coil", BusCoilItem::new
    );
    public static final RegistryObject<PunchedTapeItem> PUNCHED_TAPE = REGISTER.register(
            "punched_tape", PunchedTapeItem::new
    );
    public static final RegistryObject<EmptyTapeItem> EMPTY_TAPE = REGISTER.register("empty_tape", EmptyTapeItem::new);
    public static final RegistryObject<PanelTopItem> PANEL_TOP = REGISTER.register("panel_top", PanelTopItem::new);
    public static final Map<ResourceLocation, RegistryObject<Item>> CLOCK_GENERATORS;
    public static final RegistryObject<PCBStackItem> PCB_STACK = REGISTER.register("pcb_stack", PCBStackItem::new);

    //Blocks
    private static final RegistryObject<BlockItem> BUS_RELAY = blockItem(CEBlocks.BUS_RELAY);
    private static final RegistryObject<CEBlockItem<Direction>> BUS_INTERFACE = blockItemCE(CEBlocks.BUS_INTERFACE);
    private static final RegistryObject<BlockItem> LINE_ACCESS = blockItem(CEBlocks.LINE_ACCESS);
    private static final RegistryObject<CEBlockItem<PanelOrientation>> CONTROL_PANEL = blockItemCE(CEBlocks.CONTROL_PANEL);
    private static final RegistryObject<CEBlockItem<Direction>> TELETYPE = blockItemCE(CEBlocks.TELETYPE);
    private static final RegistryObject<CEBlockItem<Direction>> PANEL_CNC = blockItemCE(CEBlocks.PANEL_CNC);
    private static final RegistryObject<CEBlockItem<Direction>> LOGIC_CABINET = blockItemCE(CEBlocks.LOGIC_CABINET);
    private static final RegistryObject<CEBlockItem<Direction>> LOGIC_WORKBENCH = blockItemCE(CEBlocks.LOGIC_WORKBENCH);
    private static final RegistryObject<CEBlockItem<Unit>> PANEL_DESIGNER = blockItemCE(CEBlocks.PANEL_DESIGNER);

    //TODO remove
    private static RegistryObject<BlockItem> blockItem(RegistryObject<? extends Block> block) {
        return REGISTER.register(block.getId().getPath(), () -> new BlockItem(block.get(), simpleItemProperties()));
    }

    private static <T> RegistryObject<CEBlockItem<T>> blockItemCE(RegistryObject<? extends CEBlock<T, ?>> block) {
        return REGISTER.register(block.getId().getPath(), () -> new CEBlockItem<>(block.get(), simpleItemProperties()));
    }

    private static Item.Properties simpleItemProperties() {
        return new Item.Properties().group(ControlEngineering.ITEM_GROUP);
    }

    static {
        ImmutableMap.Builder<ResourceLocation, RegistryObject<Item>> clockSources = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, ClockGenerator<?>> entry : ClockTypes.getGenerators().entrySet()) {
            ResourceLocation id = entry.getKey();
            if (entry.getValue().isActiveClock()) {
                clockSources.put(
                        id, REGISTER.register(id.getPath(), () -> new Item(simpleItemProperties()))
                );
            }
        }
        CLOCK_GENERATORS = clockSources.build();
    }
}
