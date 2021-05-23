package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<PanelComponentType<?, ?>, ComponentRenderer<?, ?>> RENDERS = new HashMap<>();

    public static void init() {
        register(PanelComponents.BUTTON, new ButtonRender());
        register(PanelComponents.INDICATOR, new IndicatorRender());
        register(PanelComponents.LABEL, new LabelRender());
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
            MatrixStack transform,
            RenderType... staticTypes
    ) {
        MixedModel result = new MixedModel(staticTypes);
        for (PlacedComponent component : components) {
            transform.push();
            transform.translate(component.getPosMin().x, 0, component.getPosMin().y);
            render(result, component.getComponent(), transform);
            transform.pop();
        }
        return result;
    }

    public static <Config, State> void render(
            MixedModel out, PanelComponentInstance<Config, State> instance, MatrixStack transform
    ) {
        getRenderer(instance.getType()).render(out, instance.getConfig(), instance.getState(), transform);
    }
}
