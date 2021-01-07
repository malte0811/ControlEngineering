package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.controlpanels.PanelComponent;
import malte0811.controlengineering.controlpanels.renders.target.RenderTarget;
import net.minecraft.client.renderer.LightTexture;

public abstract class ComponentRenderer<T extends PanelComponent<T>> {
    protected static final int FULLBRIGHT = LightTexture.packLight(15, 15);

    public abstract void render(
            RenderTarget output,
            T instance,
            MatrixStack transform
    );
}
