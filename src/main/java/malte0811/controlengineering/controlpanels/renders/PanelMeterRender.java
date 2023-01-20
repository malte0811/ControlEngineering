package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.client.render.utils.TransformingVertexBuilder;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

import static malte0811.controlengineering.controlpanels.components.PanelMeter.SIZE;

public class PanelMeterRender implements ComponentRenderer<BusSignalRef, Integer> {
    private static final List<Marker> MARKERS = Util.make(new ArrayList<>(), list -> {
        for (int i = 0; i < 4; ++i) {
            var strength = i * BusLine.MAX_VALID_VALUE / 3;
            list.add(new Marker(Integer.toString(strength), rotationFor(strength)));
        }
    });
    private static final Quaternionf MARKER_FONT_ROTATION = new Quaternionf()
            .rotateY(Mth.PI)
            .rotateX(Mth.HALF_PI);
    private static final double AXIS_X = SIZE.x() / 2;
    private static final double AXIS_Y = SIZE.y() / 6;
    private static final double NEEDLE_LENGTH = SIZE.y() - 2 * AXIS_Y;
    private static final double NEEDLE_WIDTH = NEEDLE_LENGTH / 10;

    @Override
    public void render(MixedModel output, BusSignalRef line, Integer strength, PoseStack transform) {
        ComponentRenderer.renderBase(output, transform, SIZE, -1);
        transform.pushPose();
        transform.translate(AXIS_X, 2 * EPSILON, SIZE.y() - AXIS_Y);
        renderMarkers(output, transform);
        renderNeedle(output, transform, strength);
        transform.popPose();
    }

    private void renderMarkers(MixedModel output, PoseStack transform) {
        var font = Minecraft.getInstance().font;
        for (var marker : MARKERS) {
            transform.pushPose();
            transform.mulPose(marker.angle());
            transform.translate(0, 0, NEEDLE_LENGTH);
            transform.mulPose(MARKER_FONT_ROTATION);
            final var scale = 1 / 16f;
            transform.scale(scale, scale, scale);
            font.drawInBatch(
                    marker.desc(),
                    -font.width(marker.desc()) / 2f, -font.lineHeight / 2f,
                    0, false, transform.last().pose(), output, false, 0, 0
            );
            transform.popPose();
        }
    }

    private void renderNeedle(MixedModel output, PoseStack transform, int strength) {
        transform.mulPose(rotationFor(strength));
        new QuadBuilder(
                new Vec3(-NEEDLE_WIDTH / 2, 0, NEEDLE_LENGTH),
                new Vec3(NEEDLE_WIDTH / 2, 0, NEEDLE_LENGTH),
                new Vec3(NEEDLE_WIDTH / 2, 0, 0),
                new Vec3(-NEEDLE_WIDTH / 2, 0, 0)
        ).setRGB(0).writeTo(new TransformingVertexBuilder(output, MixedModel.SOLID_DYNAMIC, transform));
    }

    private static Quaternionf rotationFor(int signalStrength) {
        var relative = signalStrength / (float) BusLine.MAX_VALID_VALUE;
        var totalAngle = 2 / 3f * Mth.PI;
        var angleFromVertical = (relative - 0.5f) * totalAngle;
        return new Quaternionf().rotateY(Mth.PI - angleFromVertical);
    }

    private record Marker(String desc, Quaternionf angle) { }
}
