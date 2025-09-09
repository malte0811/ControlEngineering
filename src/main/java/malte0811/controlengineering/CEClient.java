package malte0811.controlengineering;

import malte0811.controlengineering.blockentity.CEBlockEntities;
import malte0811.controlengineering.client.manual.CEManual;
import malte0811.controlengineering.client.render.panel.PanelCNCRenderer;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.client.render.tape.SequencerRenderer;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT)
public class CEClient {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent ev) {
        ComponentRenderers.init();
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
