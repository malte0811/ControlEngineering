package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelComponent;

public abstract class ComponentRenderer<T extends PanelComponent<T>> {
    public abstract void render(IVertexBuilder builder, T instance, MatrixStack transform);
}
