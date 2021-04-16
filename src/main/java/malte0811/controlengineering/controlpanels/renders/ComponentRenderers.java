package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.RenderTarget;
import malte0811.controlengineering.controlpanels.PanelComponentInstance;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<PanelComponentType<?, ?>, ComponentRenderer<?, ?>> RENDERS = new HashMap<>();

    public static void init() {
        register(PanelComponents.BUTTON, new ButtonRender());
        register(PanelComponents.INDICATOR, new IndicatorRender());
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

    public static void renderAll(RenderTarget target, List<PlacedComponent> components, MatrixStack transform) {
        for (PlacedComponent component : components) {
            transform.push();
            transform.translate(component.getPosMin().x, 0, component.getPosMin().y);
            ComponentRenderers.render(target, component.getComponent(), transform);
            transform.pop();
        }
    }

    public static <Config, State> void render(
            RenderTarget builder, PanelComponentInstance<Config, State> instance, MatrixStack transform
    ) {
        getRenderer(instance.getType()).render(builder, instance.getConfig(), instance.getState(), transform);
    }
}
