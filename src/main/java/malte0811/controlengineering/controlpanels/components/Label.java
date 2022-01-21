package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.util.ServerFontWidth;
import malte0811.controlengineering.util.math.Vec2d;

public class Label extends PanelComponentType<ColorAndText, Unit> {
    public static final int FONT_HEIGHT = 9;
    public static final float SCALE = 1f / FONT_HEIGHT;

    public Label() {
        super(
                ColorAndText.DEFAULT, Unit.INSTANCE,
                ColorAndText.CODEC, Codec.unit(Unit.INSTANCE),
                null, 0
        );
    }

    @Override
    public Vec2d getSize(ColorAndText s) {
        // TODO handle client-side on dedicated servers?
        return new Vec2d(SCALE * ServerFontWidth.getWidth(s.text()), 1);
    }
}
