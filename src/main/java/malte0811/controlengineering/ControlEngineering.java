package malte0811.controlengineering;


import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.items.IEItems;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.bus.BlockRenderLayers;
import malte0811.controlengineering.bus.BusWireTypes;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.temp.ImprovedLocalRSHandler;
import malte0811.controlengineering.tiles.CETileEntities;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(ControlEngineering.MODID)
@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ControlEngineering {
    public static final String MODID = "controlengineering";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Nonnull
        public ItemStack createIcon() {
            //TODO
            return new ItemStack(IEItems.Misc.wireCoils.get(WireType.COPPER));
        }
    };

    public ControlEngineering() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CEBlocks.REGISTER.register(modBus);
        CETileEntities.REGISTER.register(modBus);
        CEItems.REGISTER.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener(this::loadComplete);
    }

    public void setup(FMLCommonSetupEvent ev) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> BlockRenderLayers::init);
        LocalNetworkHandler.register(LocalBusHandler.NAME, LocalBusHandler::new);
        BusWireTypes.init();
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        LocalNetworkHandler.register(RedstoneNetworkHandler.ID, ImprovedLocalRSHandler::new);
    }
}
