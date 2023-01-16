package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.controlpanels.components.Label;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class LabelRender implements ComponentRenderer<ColorAndText, Unit> {
    private static final Quaternionf ROTATION = new Quaternionf()
            .scale(Label.SCALE)
            .rotationX(Mth.HALF_PI);

    @Override
    public void render(MixedModel output, ColorAndText s, Unit unit, PoseStack transform) {
        transform.pushPose();
        transform.translate(0, 1e-3, 0);
        transform.mulPose(ROTATION);
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
