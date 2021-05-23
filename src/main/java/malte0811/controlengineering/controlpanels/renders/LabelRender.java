package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.components.Label;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Quaternion;

public class LabelRender extends ComponentRenderer<String, Unit> {
    @Override
    public void render(MixedModel output, String s, Unit unit, MatrixStack transform) {
        transform.push();
        transform.translate(0, 1e-3, 0);
        transform.scale(Label.SCALE, Label.SCALE, Label.SCALE);
        transform.rotate(new Quaternion(90, 0, 0, true));
        Minecraft.getInstance().fontRenderer.renderString(
                //TODO color config
                s, 0, 0, -1, false, transform.getLast().getMatrix(), output, false, 0, 0
        );
        transform.pop();
    }
}
