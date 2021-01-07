package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.controlpanels.PanelComponent;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.renders.target.RenderTarget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<PanelComponentType<?>, ComponentRenderer<?>> RENDERS = new HashMap<>();

    public static void init() {
        register(PanelComponents.BUTTON, new ButtonRender());
        register(PanelComponents.INDICATOR, new IndicatorRender());
    }

    public static <T extends PanelComponent<T>> void register(
            PanelComponentType<? extends T> type,
            ComponentRenderer<? super T> renderer
    ) {
        RENDERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PanelComponent<?>> ComponentRenderer<? super T> getRenderer(T instance) {
        return (ComponentRenderer<? super T>) RENDERS.get(instance.getType());
    }

    public static void renderAll(RenderTarget target, List<PlacedComponent> components, MatrixStack transform) {
        for (PlacedComponent component : components) {
            transform.push();
            transform.translate(component.getPosMin().x, 0, component.getPosMin().y);
            ComponentRenderers.render(target, component.getComponent(), transform);
            transform.pop();
        }
    }

    public static <T extends PanelComponent<?>> void render(
            RenderTarget builder,
            T instance,
            MatrixStack transform
    ) {
        getRenderer(instance).render(builder, instance, transform);
    }
}
