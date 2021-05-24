package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.components.Label;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Quaternion;

public class LabelRender extends ComponentRenderer<ColorAndText, Unit> {
    @Override
    public void render(MixedModel output, ColorAndText s, Unit unit, MatrixStack transform) {
        transform.push();
        transform.translate(0, 1e-3, 0);
        transform.scale(Label.SCALE, Label.SCALE, Label.SCALE);
        transform.rotate(new Quaternion(90, 0, 0, true));
        Minecraft.getInstance().fontRenderer.renderString(
                s.getText(),
                0, 0,
                s.getColor(), false,
                transform.getLast().getMatrix(), output,
                false, 0, 0
        );
        transform.pop();
    }
}
