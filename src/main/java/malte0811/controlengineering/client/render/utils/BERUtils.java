package malte0811.controlengineering.client.render.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

public class BERUtils {
    public static void rotateAroundCenter(double angleDegrees, PoseStack stack) {
        stack.translate(.5, .5, .5);
        stack.mulPose(new Quaternion(0, (float) angleDegrees, 0, true));
        stack.translate(-.5, -.5, -.5);
    }
}
