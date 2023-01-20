package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CECreativeTab {
    private static CreativeModeTab TAB;

    @SubscribeEvent
    public static void createTab(CreativeModeTabEvent.Register ev) {
        TAB = ev.registerCreativeModeTab(
                new ResourceLocation(ControlEngineering.MODID, "main"),
                builder -> builder.icon(() -> new ItemStack(CEBlocks.LOGIC_CABINET.get()))
                        .title(Component.literal(ControlEngineering.MODNAME))
        );
    }

    @SubscribeEvent
    public static void populateTab(CreativeModeTabEvent.BuildContents ev) {
        if (ev.getTab() != TAB) {
            return;
        }
        for (final var itemRO : CEItems.REGISTER.getEntries()) {
            if (itemRO.equals(CEItems.CONTROL_PANEL)) { continue; }
            if (itemRO.equals(CEItems.PCB_STACK)) { continue; }
            if (itemRO.equals(CEItems.EMPTY_TAPE)) {
                ev.accept(EmptyTapeItem.withLength(16));
                ev.accept(EmptyTapeItem.withLength(256));
            } else if (itemRO.equals(CEItems.PUNCHED_TAPE)) {
                final var item = itemRO.get();
                ev.accept(PunchedTapeItem.setBytes(new ItemStack(item), BitUtils.toBytesWithParity("Test1")));
                ev.accept(PunchedTapeItem.setBytes(new ItemStack(item), BitUtils.toBytesWithParity("Another test")));
            } else {
                ev.accept(itemRO);
            }
        }
    }
}
