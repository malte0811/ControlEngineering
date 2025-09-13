package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID)
public class CECreativeTab {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB, ControlEngineering.MODID
    );

    public static final Supplier<CreativeModeTab> CE_TAB = REGISTER.register(ControlEngineering.MODID,
            () -> CreativeModeTab.builder()
                    // Set name of tab to display
                    .title(Component.literal(ControlEngineering.MODNAME))
                    // Set icon of creative tab
                    .icon(() -> new ItemStack(CEBlocks.LOGIC_CABINET.get()))
                    .build()
    );

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent ev) {
        if (ev.getTab() != CE_TAB.get()) {
            return;
        }
        for (final var itemRO : CEItems.REGISTER.getEntries()) {
            if (itemRO.equals(CEItems.CONTROL_PANEL)) {
                continue;
            }
            if (itemRO.equals(CEItems.PCB_STACK)) {
                continue;
            }
            if (itemRO.equals(CEItems.EMPTY_TAPE)) {
                ev.accept(EmptyTapeItem.withLength(16));
                ev.accept(EmptyTapeItem.withLength(256));
            } else if (itemRO.equals(CEItems.PUNCHED_TAPE)) {
                final var item = itemRO.get();
                ev.accept(PunchedTapeItem.setBytes(new ItemStack(item), BitUtils.toBytesWithParity("Test1")));
                ev.accept(PunchedTapeItem.setBytes(new ItemStack(item), BitUtils.toBytesWithParity("Another test")));
            } else {
                ev.accept(itemRO.get());
            }
        }
    }
}
