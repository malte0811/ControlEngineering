package malte0811.controlengineering;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.blocks.BlockRenderLayers;
import malte0811.controlengineering.client.manual.CEManual;
import malte0811.controlengineering.client.render.panel.PanelCNCRenderer;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.client.render.tape.SequencerRenderer;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEClient {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent ev) {
        ComponentRenderers.init();
        ContainerScreenManager.registerScreens();
        BlockRenderLayers.init();
        CEManual.initManual();
        ClientSymbols.init();
    }

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers ev) {
        ev.registerBlockEntityRenderer(CEBlockEntities.CONTROL_PANEL.dummy().get(), PanelRenderer::new);
        ev.registerBlockEntityRenderer(CEBlockEntities.PANEL_CNC.master().get(), PanelCNCRenderer::new);
        ev.registerBlockEntityRenderer(CEBlockEntities.SEQUENCER.get(), SequencerRenderer::new);
    }
}
