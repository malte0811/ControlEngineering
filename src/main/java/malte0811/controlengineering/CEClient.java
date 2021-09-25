package malte0811.controlengineering;

import malte0811.controlengineering.blocks.BlockRenderLayers;
import malte0811.controlengineering.controlpanels.renders.ComponentRenderers;
import malte0811.controlengineering.controlpanels.renders.PanelCNCRenderer;
import malte0811.controlengineering.controlpanels.renders.PanelRenderer;
import malte0811.controlengineering.gui.ContainerScreenManager;
import malte0811.controlengineering.tiles.CETileEntities;
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
    }

    @SubscribeEvent
    public static void registerBERs(EntityRenderersEvent.RegisterRenderers ev) {
        ev.registerBlockEntityRenderer(CETileEntities.CONTROL_PANEL.get(), PanelRenderer::new);
        ev.registerBlockEntityRenderer(CETileEntities.PANEL_CNC.get(), PanelCNCRenderer::new);
    }
}
