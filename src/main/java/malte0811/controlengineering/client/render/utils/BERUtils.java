package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class BERUtils {
    public static void rotateAroundCenter(double angleDegrees, PoseStack stack) {
        stack.translate(.5, .5, .5);
        stack.mulPose(new Quaternionf().rotateY((float) (Mth.DEG_TO_RAD * angleDegrees)));
        stack.translate(-.5, -.5, -.5);
    }
}
