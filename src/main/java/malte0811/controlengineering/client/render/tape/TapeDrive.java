package malte0811.controlengineering.client.render.tape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import malte0811.controlengineering.util.math.Vec2d;

public class TapeDrive {
    private final double totalLength;
    private final double lambda;
    private final double emptyRadius;
    private final double fullRadius;
    private final TapeWheel leftWheel;
    private final Vec2d leftCenter;
    private final TapeWheel rightWheel;
    private final Vec2d rightCenter;

    public TapeDrive(
            double totalLength, double fullRadius, double emptyRadius,
            Vec2d leftCenter, Vec2d leftTarget,
            Vec2d rightCenter, Vec2d rightTarget
    ) {
        this.totalLength = totalLength;
        this.fullRadius = fullRadius;
        this.leftCenter = leftCenter;
        this.leftWheel = new TapeWheel(leftTarget.subtract(leftCenter), fullRadius, false);
        this.rightCenter = rightCenter;
        this.rightWheel = new TapeWheel(rightTarget.subtract(rightCenter), fullRadius, true);
        this.lambda = (fullRadius * fullRadius - emptyRadius * emptyRadius) / totalLength;
        this.emptyRadius = emptyRadius;
        updateTapeProgress(0);
    }

    public void render(VertexConsumer output, PoseStack stack, int light, int overlay) {
        stack.pushPose();
        stack.translate(leftCenter.x, 0, leftCenter.y);
        leftWheel.render(output, stack, light, overlay);
        stack.popPose();
        stack.pushPose();
        stack.translate(rightCenter.x, 0, rightCenter.y);
        rightWheel.render(output, stack, light, overlay);
        stack.popPose();
    }

    public void updateTapeProgress(double lengthOnRight) {
        leftWheel.setRadius(getRadius(totalLength - lengthOnRight) / fullRadius);
        rightWheel.setRadius(getRadius(lengthOnRight) / fullRadius);
        //TODO these special cases can be implemented efficiently
        leftWheel.setRotationRadians(getRotationInBetween(0, totalLength - lengthOnRight));
        rightWheel.setRotationRadians(getRotationInBetween(lengthOnRight, 0));
    }

    private double getRotationInBetween(double lengthA, double lengthB) {
        //todo is this actually correct???
        return 2 / lambda * (getRadius(lengthB) - getRadius(lengthA));
    }

    private double getRadius(double length) {
        return Math.sqrt(lambda * length + emptyRadius * emptyRadius);
    }
}
