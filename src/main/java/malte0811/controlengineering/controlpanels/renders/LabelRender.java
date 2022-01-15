package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Unit;
import com.mojang.math.Quaternion;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.components.Label;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import net.minecraft.client.Minecraft;

public class LabelRender implements ComponentRenderer<ColorAndText, Unit> {
    @Override
    public void render(MixedModel output, ColorAndText s, Unit unit, PoseStack transform) {
        transform.pushPose();
        transform.translate(0, 1e-3, 0);
        transform.scale(Label.SCALE, Label.SCALE, Label.SCALE);
        transform.mulPose(new Quaternion(90, 0, 0, true));
        Minecraft.getInstance().font.drawInBatch(
                s.text(),
                0, 0,
                s.color(), false,
                transform.last().pose(), output,
                false, 0, 0
        );
        transform.popPose();
    }
}
