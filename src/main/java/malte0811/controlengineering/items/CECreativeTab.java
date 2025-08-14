package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class CECreativeTab {
    public static final DeferredRegister<CreativeModeTab> REGISTRER = DeferredRegister.create(
           Registries.CREATIVE_MODE_TAB, ControlEngineering.MODID
    );

    public static final RegistryObject<CreativeModeTab> CE_TAB = REGISTRER.register(ControlEngineering.MODID, () -> CreativeModeTab.builder()
            // Set name of tab to display
            .title(Component.literal(ControlEngineering.MODNAME))
            // Set icon of creative tab
//            .icon(() -> new ItemStack(CEBlocks.LOGIC_CABINET.get()))
            .icon(() -> new ItemStack(CEItems.EMPTY_TAPE.get()))
            .build()
          );

    @SubscribeEvent
    public static void populateTab(BuildCreativeModeTabContentsEvent ev) {
        if (ev.getTab()    !=  CE_TAB.get()) {
            return;
        }
        for (final var itemRO : CEItems.REGISTER.getEntries()) {
            if (itemRO.equals(CEItems.CONTROL_PANEL)) { continue; }
//            if (itemRO.equals(CEItems.PCB_STACK)) { continue; }
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
