package malte0811.controlengineering.items;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.bus.BusWireTypes;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class CEItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            ControlEngineering.MODID
    );

    private static final List<RegistryObject<BusCoilItem>> BUS_WIRE_COILS;
    private static final RegistryObject<BlockItem> BUS_RELAY = blockItem(CEBlocks.BUS_RELAY);
    private static final RegistryObject<BlockItem> BUS_INTERFACE = blockItem(CEBlocks.BUS_INTERFACE);
    private static final RegistryObject<BlockItem> LINE_ACCESS = blockItem(CEBlocks.LINE_ACCESS);
    private static final RegistryObject<CEBlockItem<PanelOrientation>> CONTROL_PANEL = blockItemCE(CEBlocks.CONTROL_PANEL);

    @Nonnull
    public static BusCoilItem getBusCoil(int width) {
        Preconditions.checkArgument(
                width >= BusWireTypes.MIN_BUS_WIDTH && width <= BusWireTypes.MAX_BUS_WIDTH,
                "Unexpected bus width %s",
                width
        );
        return BUS_WIRE_COILS.get(width - BusWireTypes.MIN_BUS_WIDTH).get();
    }

    //TODO remove
    private static RegistryObject<BlockItem> blockItem(RegistryObject<? extends Block> block) {
        return REGISTER.register(
                block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties().group(ControlEngineering.ITEM_GROUP))
        );
    }

    private static <T> RegistryObject<CEBlockItem<T>> blockItemCE(RegistryObject<? extends CEBlock<T>> block) {
        return REGISTER.register(
                block.getId().getPath(),
                () -> new CEBlockItem<>(block.get(), new Item.Properties().group(ControlEngineering.ITEM_GROUP))
        );
    }

    static {
        ImmutableList.Builder<RegistryObject<BusCoilItem>> busWireCoils = ImmutableList.builder();
        for (int width = BusWireTypes.MIN_BUS_WIDTH; width <= BusWireTypes.MAX_BUS_WIDTH; ++width) {
            final int finalWidth = width;
            busWireCoils.add(REGISTER.register(
                    "bus_wire_coil_" + width,
                    () -> new BusCoilItem(finalWidth)
            ));
        }
        BUS_WIRE_COILS = busWireCoils.build();
    }
}
