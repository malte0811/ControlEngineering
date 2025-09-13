package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.bus.BusWireType;
import malte0811.controlengineering.bus.LocalBusHandler;
import malte0811.controlengineering.crafting.CERecipeSerializers;
import malte0811.controlengineering.crafting.CERecipeTypes;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.itemdata.CEDataComponents;
import malte0811.controlengineering.items.CECreativeTab;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.loot.BlueprintChestModifier;
import malte0811.controlengineering.loot.CELootFunctions;
import malte0811.controlengineering.network.CutTapePacket;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubpackets;
import malte0811.controlengineering.network.logic.LogicPacket;
import malte0811.controlengineering.network.logic.LogicSubPackets;
import malte0811.controlengineering.network.panellayout.PanelPacket;
import malte0811.controlengineering.network.panellayout.PanelSubPackets;
import malte0811.controlengineering.network.remapper.RemapperPacket;
import malte0811.controlengineering.network.remapper.RemapperSubPacket;
import malte0811.controlengineering.network.scope.ScopePacket;
import malte0811.controlengineering.network.scope.ScopeSubPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ControlEngineering.MODID)
@EventBusSubscriber
public class ControlEngineering {
    public static final String MODID = "controlengineering";
    public static final String MODNAME = "Control Engineering";
    public static final Logger LOGGER = LogManager.getLogger();

    public ControlEngineering(IEventBus modBus) {
        CEBlocks.REGISTER.register(modBus);
        CEBlockEntities.REGISTER.register(modBus);
        CEItems.REGISTER.register(modBus);
        CEContainers.REGISTER.register(modBus);
        CERecipeSerializers.REGISTER.register(modBus);
        CELootFunctions.REGISTER.register(modBus);
        CERecipeTypes.REGISTER.register(modBus);
        BlueprintChestModifier.REGISTER.register(modBus);
        CECreativeTab.REGISTER.register(modBus);
        CEDataComponents.REGISTER.register(modBus);
        IEItemRefs.init();
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent ev) {
        LocalNetworkHandler.register(LocalBusHandler.NAME, LocalBusHandler::new);
        BusWireType.init();
    }

    @SubscribeEvent
    public static void setupNetwork(RegisterPayloadHandlersEvent ev) {
        final var registrar = ev.registrar(MODID);
        KeypunchSubpackets.init();
        LogicSubPackets.init();
        PanelSubPackets.init();
        RemapperSubPacket.init();
        ScopeSubPacket.init();
        registrar.playBidirectional(KeypunchPacket.ID, KeypunchPacket.CODEC, KeypunchPacket::process);
        registrar.playBidirectional(LogicPacket.ID, LogicPacket.CODEC, LogicPacket::process);
        registrar.playBidirectional(PanelPacket.ID, PanelPacket.CODEC, PanelPacket::process);
        registrar.playBidirectional(RemapperPacket.ID, RemapperPacket.CODEC, RemapperPacket::process);
        registrar.playBidirectional(ScopePacket.ID, ScopePacket.CODEC, ScopePacket::process);
        registrar.playToServer(CutTapePacket.ID, CutTapePacket.CODEC, CutTapePacket::process);
    }
}
