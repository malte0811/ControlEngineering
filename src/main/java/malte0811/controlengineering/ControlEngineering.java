package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.cells.Leafcells;
import malte0811.controlengineering.network.CutTapePacket;
import malte0811.controlengineering.network.SimplePacket;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.panellayout.PanelPacket;
import malte0811.controlengineering.network.remapper.RSRemapperPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
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

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID) {
        @Nonnull
        public ItemStack makeIcon() {
            return new ItemStack(CEBlocks.LOGIC_CABINET.get());
        }
    };

    public ControlEngineering() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CEBlocks.REGISTER.register(modBus);
        CEBlockEntities.REGISTER.register(modBus);
        CEItems.REGISTER.register(modBus);
        CEContainers.REGISTER.register(modBus);
        CERecipeSerializers.REGISTER.register(modBus);
        modBus.addListener(this::setup);
        Leafcells.init();
        IEItemRefs.init();
    }

    public void setup(FMLCommonSetupEvent ev) {
        LocalNetworkHandler.register(LocalBusHandler.NAME, LocalBusHandler::new);
        BusWireType.init();
        registerPackets();
    }

    private void registerPackets() {
        int id = 0;
        registerPacket(id++, KeypunchPacket.class, KeypunchPacket::new);
        registerPacket(id++, LogicPacket.class, LogicPacket::new);
        registerPacket(id++, PanelPacket.class, PanelPacket::new);
        registerPacket(id++, RSRemapperPacket.class, RSRemapperPacket::new);
        registerPacket(id++, CutTapePacket.class, CutTapePacket::new, NetworkDirection.PLAY_TO_SERVER);
    }

    private <T extends SimplePacket> void registerPacket(
            int id, Class<T> type, Function<FriendlyByteBuf, T> read
    ) {
        NETWORK.registerMessage(id, type, T::write, read, T::process, Optional.empty());
    }

    private <T extends SimplePacket> void registerPacket(
            int id, Class<T> type, Function<FriendlyByteBuf, T> read, NetworkDirection direction
    ) {
        NETWORK.registerMessage(id, type, T::write, read, T::process, Optional.of(direction));
    }
}
