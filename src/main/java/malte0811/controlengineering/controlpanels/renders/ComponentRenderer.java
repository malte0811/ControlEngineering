package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.RenderTarget;
import net.minecraft.client.renderer.LightTexture;

public abstract class ComponentRenderer<Config, State> {
    protected static final int FULLBRIGHT = LightTexture.packLight(15, 15);

    public abstract void render(RenderTarget output, Config config, State state, MatrixStack transform);
}
