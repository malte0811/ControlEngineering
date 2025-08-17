package malte0811.controlengineering.items;

import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.logic.clock.ClockGenerator;
import malte0811.controlengineering.logic.clock.ClockTypes;
import malte0811.controlengineering.scope.module.ScopeModule;
import malte0811.controlengineering.scope.module.ScopeModules;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;
import java.util.function.Predicate;

public class CEItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(ControlEngineering.MODID);

    //Items
    public static final DeferredItem<BusCoilItem> BUS_WIRE_COIL = REGISTER.register(
            "bus_wire_coil", BusCoilItem::new
    );
    public static final DeferredItem<PunchedTapeItem> PUNCHED_TAPE = REGISTER.register(
            "punched_tape", PunchedTapeItem::new
    );
    public static final DeferredItem<EmptyTapeItem> EMPTY_TAPE = REGISTER.register("empty_tape", EmptyTapeItem::new);
    public static final DeferredItem<PanelTopItem> PANEL_TOP = REGISTER.register("panel_top", PanelTopItem::new);
    public static final DeferredItem<ItemWithKeyID> LOCK = REGISTER.register("lock", ItemWithKeyID::new);
    public static final DeferredItem<ItemWithKeyID> KEY = REGISTER.register("key", ItemWithKeyID::new);
    public static final Map<ResourceLocation, DeferredItem<Item>> CLOCK_GENERATORS;
    public static final Map<ResourceLocation, DeferredItem<Item>> SCOPE_MODULES;
    public static final DeferredItem<PCBStackItem> PCB_STACK = REGISTER.register("pcb_stack", PCBStackItem::new);
    public static final DeferredItem<SchematicItem> SCHEMATIC = REGISTER.register(
            "logic_schematic", SchematicItem::new
    );
    public static final DeferredItem<Item> CRT_TUBE = simpleItem("crt_tube");
    public static final DeferredItem<Item> SCOPE_MODULE_CASE = simpleItem("scope_module_case");

    //Blocks
    public static final DeferredItem<CEBlockItem<Direction>> BUS_RELAY = blockItemCE(CEBlocks.BUS_RELAY);
    public static final DeferredItem<CEBlockItem<Direction>> BUS_INTERFACE = blockItemCE(CEBlocks.BUS_INTERFACE);
    public static final DeferredItem<CEBlockItem<Direction>> LINE_ACCESS = blockItemCE(CEBlocks.LINE_ACCESS);
    public static final DeferredItem<CEBlockItem<PanelOrientation>> CONTROL_PANEL = REGISTER.register(
            CEBlocks.CONTROL_PANEL.getId().getPath(),
            () -> new ControlPanelItem(CEBlocks.CONTROL_PANEL.get(), simpleItemProperties())
    );
    public static final DeferredItem<CEBlockItem<Direction>> KEYPUNCH = blockItemCE(CEBlocks.KEYPUNCH);
    public static final DeferredItem<CEBlockItem<Direction>> SEQUENCER = blockItemCE(CEBlocks.SEQUENCER);
    public static final DeferredItem<CEBlockItem<Direction>> PANEL_CNC = blockItemCE(CEBlocks.PANEL_CNC);
    public static final DeferredItem<CEBlockItem<Direction>> LOGIC_CABINET = blockItemCE(CEBlocks.LOGIC_CABINET);
    public static final DeferredItem<CEBlockItem<Direction>> LOGIC_WORKBENCH = blockItemCE(CEBlocks.LOGIC_WORKBENCH);
    public static final DeferredItem<CEBlockItem<Direction>> PANEL_DESIGNER = blockItemCE(CEBlocks.PANEL_DESIGNER);
    public static final DeferredItem<CEBlockItem<Direction>> RS_REMAPPER = blockItemCE(CEBlocks.RS_REMAPPER);
    public static final DeferredItem<CEBlockItem<Direction>> SCOPE = blockItemCE(CEBlocks.SCOPE);

    private static <T> DeferredItem<CEBlockItem<T>> blockItemCE(DeferredBlock<? extends CEBlock<T>> block) {
        return blockItemCE(block, simpleItemProperties());
    }

    private static <T> DeferredItem<CEBlockItem<T>> blockItemCE(
            DeferredBlock<? extends CEBlock<T>> block, Item.Properties properties
    ) {
        return REGISTER.register(block.getId().getPath(), () -> new CEBlockItem<>(block.get(), properties));
    }

    private static DeferredItem<Item> simpleItem(String name) {
        return REGISTER.register(name, () -> new Item(simpleItemProperties()));
    }

    public static Item.Properties simpleItemProperties() {
        return new Item.Properties();
    }

    private static <T>
    Map<ResourceLocation, DeferredItem<Item>> makeItemsFor(
            Map<ResourceLocation, T> owners, Predicate<T> shouldAdd, String prefix
    ) {
        ImmutableMap.Builder<ResourceLocation, DeferredItem<Item>> items = ImmutableMap.builder();
        for (Map.Entry<ResourceLocation, T> entry : owners.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (shouldAdd.test(entry.getValue())) {
                items.put(id, REGISTER.register(prefix + id.getPath(), () -> new Item(simpleItemProperties())));
            }
        }
        return items.build();
    }

    static {
        CLOCK_GENERATORS = makeItemsFor(
                ClockTypes.getGenerators(), ClockGenerator::isActiveClock, ClockTypes.ITEM_KEY_PREFIX
        );
        SCOPE_MODULES = makeItemsFor(
                ScopeModules.REGISTRY.getEntries(), Predicate.not(ScopeModule::isEmpty), ScopeModules.ITEM_PREFIX
        );
    }
}
