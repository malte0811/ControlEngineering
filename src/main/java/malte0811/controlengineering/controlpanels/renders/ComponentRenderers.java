package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.components.TimedButton;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<PanelComponentType<?, ?>, ComponentRenderer<?, ?>> RENDERS = new HashMap<>();

    public static void init() {
        register(PanelComponents.BUTTON, new ButtonRender<>(b -> b));
        register(PanelComponents.INDICATOR, new IndicatorRender());
        register(PanelComponents.LABEL, new LabelRender());
        register(PanelComponents.TOGGLE_SWITCH, new SwitchRender());
        register(PanelComponents.COVERED_SWITCH, new CoveredSwitchRender());
        register(PanelComponents.TIMED_BUTTON, new ButtonRender<>(TimedButton::isActive));
        register(PanelComponents.PANEL_METER, new PanelMeterRender());
        register(PanelComponents.VARIAC, new VariacRender());
        register(PanelComponents.SLIDER_HOR, new SliderRender(true));
        register(PanelComponents.SLIDER_VERT, new SliderRender(false));
    }

    public static <Config, State> void register(
            PanelComponentType<Config, State> type, ComponentRenderer<Config, State> renderer
    ) {
        RENDERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <Config, State> ComponentRenderer<Config, State> getRenderer(PanelComponentType<Config, State> type) {
        return (ComponentRenderer<Config, State>) RENDERS.get(type);
    }

    public static MixedModel renderAll(
            List<PlacedComponent> components,
            PoseStack transform,
            RenderType... staticTypes
    ) {
        MixedModel result = new MixedModel(staticTypes);
        for (PlacedComponent component : components) {
            transform.pushPose();
            transform.translate(component.getPosMin().x(), 0, component.getPosMin().y());
            render(result, component.getComponent(), transform);
            transform.popPose();
        }
        return result;
    }

    public static <Config, State> void render(
            MixedModel out, PanelComponentInstance<Config, State> instance, PoseStack transform
    ) {
        getRenderer(instance.getType()).render(out, instance.getConfig(), instance.getState(), transform);
    }
}
