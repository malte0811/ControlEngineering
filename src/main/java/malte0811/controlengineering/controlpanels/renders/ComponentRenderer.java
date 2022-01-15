package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.MixedModel;

public interface ComponentRenderer<Config, State> {
    void render(MixedModel output, Config config, State state, PoseStack transform);
}
