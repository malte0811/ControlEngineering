package malte0811.controlengineering.client;


import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.client.model.CacheableCompositeModel;
import malte0811.controlengineering.client.model.SpecialModelLoader;
import malte0811.controlengineering.client.model.logic.DynamicLogicModelLoader;
import malte0811.controlengineering.client.model.logic.LogicWorkbenchModel;
import malte0811.controlengineering.client.model.panel.PanelModel;
import malte0811.controlengineering.client.model.scope.ScopeModelLoader;
import malte0811.controlengineering.client.model.tape.KeypunchSwitchModel;
import malte0811.controlengineering.client.model.tape.SequencerSwitchModel;
import malte0811.controlengineering.client.render.panel.PanelRenderer;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelLoaders {
    public static final ResourceLocation PANEL_MODEL = RLUtils.ceLoc("panel");
    public static final ResourceLocation LOGIC_CABINET = RLUtils.ceLoc("dynamic_logic");
    public static final ResourceLocation KEYPUNCH_SWITCH = RLUtils.ceLoc("keypunch_switch");
    public static final ResourceLocation SEQUENCER_SWITCH = RLUtils.ceLoc("sequencer_switch");
    public static final ResourceLocation LOGIC_WORKBENCH = RLUtils.ceLoc("logic_workbench");
    public static final ResourceLocation CACHED_COMPOSITE = RLUtils.ceLoc("cacheable_composite");
    public static final ResourceLocation SCOPE = RLUtils.ceLoc("scope");

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders ev) {
        ev.register(PANEL_MODEL.getPath(), new SpecialModelLoader(
                PanelModel::new, PanelRenderer.PANEL_TEXTURE_LOC
        ));
        ev.register(KEYPUNCH_SWITCH.getPath(), new SpecialModelLoader(
                KeypunchSwitchModel::new, KeypunchSwitchModel.TEXTURE_LOC
        ));
        ev.register(SEQUENCER_SWITCH.getPath(), new SpecialModelLoader(
                SequencerSwitchModel::new, SequencerSwitchModel.TEXTURE_LOC
        ));
        ev.register(LOGIC_CABINET.getPath(), new DynamicLogicModelLoader());
        ev.register(LOGIC_WORKBENCH.getPath(), new LogicWorkbenchModel.Loader());
        ev.register(CACHED_COMPOSITE.getPath(), new CacheableCompositeModel.Loader());
        ev.register(SCOPE.getPath(), new ScopeModelLoader());
    }
}
