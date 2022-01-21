package malte0811.controlengineering.controlpanels.renders;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.client.render.target.MixedModel;
import malte0811.controlengineering.client.render.target.RenderUtils;
import malte0811.controlengineering.controlpanels.components.Slider;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

public class SliderRender implements ComponentRenderer<ColorAndSignal, Integer> {
    private static final Vec3 KNOB_MAX = new Vec3(Slider.KNOB_SIZE, Slider.KNOB_HEIGHT, Slider.KNOB_SIZE);
    private final boolean horizontal;

    public SliderRender(boolean horizontal) {
        this.horizontal = horizontal;
    }

    @Override
    public void render(MixedModel output, ColorAndSignal config, Integer strength, PoseStack transform) {
        ComponentRenderer.renderBase(output, transform, Slider.getSize(horizontal), 0x808080);
        var knobCenter = Mth.lerp(strength / (double) BusLine.MAX_VALID_VALUE, Slider.MIN_CENTER, Slider.MAX_CENTER);
        transform.pushPose();
        if (horizontal) {
            transform.translate(Slider.LENGTH - knobCenter - Slider.KNOB_SIZE / 2, 0, 0);
        } else {
            transform.translate(0, 0, knobCenter - Slider.KNOB_SIZE / 2);
        }

        EnumMap<Direction, Integer> colors = new EnumMap<>(Direction.class);
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            colors.put(d, -1);
        }
        var lightOverrides = Map.of(Direction.UP, IndicatorRender.lightForStrength(strength));
        colors.put(Direction.UP, IndicatorRender.colorForStrength(config.color(), strength));
        RenderUtils.renderColoredBox(
                output, transform, Vec3.ZERO, KNOB_MAX, colors, lightOverrides, RenderUtils.ALL_DYNAMIC
        );
        transform.popPose();
    }
}
