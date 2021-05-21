package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.client.render.target.MixedModel;

public abstract class ComponentRenderer<Config, State> {
    public abstract void render(MixedModel output, Config config, State state, MatrixStack transform);
}
