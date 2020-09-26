package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelComponent;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.PanelComponents;

import java.util.HashMap;
import java.util.Map;

public class ComponentRenderers {
    private static final Map<PanelComponentType<?>, ComponentRenderer<?>> RENDERS = new HashMap<>();

    public static void init() {
        register(PanelComponents.BUTTON, new ButtonRender());
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

    public static <T extends PanelComponent<?>> void render(IVertexBuilder builder, T instance, MatrixStack transform) {
        getRenderer(instance).render(builder, instance, transform);
    }
}
