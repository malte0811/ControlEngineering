package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import malte0811.controlengineering.controlpanels.PanelComponent;
import net.minecraft.client.renderer.LightTexture;

public abstract class ComponentRenderer<T extends PanelComponent<T>> {
    protected static final int FULLBRIGHT = LightTexture.packLight(15, 15);

    public abstract void render(
            IVertexBuilder builder,
            T instance,
            MatrixStack transform,
            int packedLightIn,
            int packedOverlayIn
    );
}
