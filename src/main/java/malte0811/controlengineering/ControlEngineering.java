package malte0811.controlengineering;


import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.api.wires.redstone.RedstoneNetworkHandler;
import blusunrize.immersiveengineering.common.items.IEItems;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.bus.BusWireTypes;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.network.AddTTYData;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.temp.ImprovedLocalRSHandler;
import malte0811.controlengineering.tiles.CETileEntities;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

@Mod(ControlEngineering.MODID)
@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ControlEngineering {
    public static final String MODID = "controlengineering";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String VERSION = "1.0.0";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "channel"), () -> VERSION, VERSION::equals, VERSION::equals
    );

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
        CEContainers.REGISTER.register(modBus);
        modBus.addListener(this::setup);
        modBus.addListener(this::loadComplete);
    }

    public void setup(FMLCommonSetupEvent ev) {
        LocalNetworkHandler.register(LocalBusHandler.NAME, LocalBusHandler::new);
        BusWireTypes.init();
        registerPackets();
    }

    public void loadComplete(FMLLoadCompleteEvent ev) {
        LocalNetworkHandler.register(RedstoneNetworkHandler.ID, ImprovedLocalRSHandler::new);
    }

    private void registerPackets() {
        int id = 0;
        registerPacket(id++, AddTTYData.class, AddTTYData::new, NetworkDirection.PLAY_TO_SERVER);
    }

    private <T extends SimplePacket> void registerPacket(
            int id, Class<T> type, Function<PacketBuffer, T> read, NetworkDirection direction
    ) {
        NETWORK.registerMessage(id, type, T::write, read, T::process, Optional.of(direction));
    }
}
