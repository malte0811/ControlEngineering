package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.util.ServerFontWidth;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;

public class Label extends PanelComponentType<ColorAndText, Unit> {
    public static final int FONT_HEIGHT = 9;
    public static final float SCALE = 1f / FONT_HEIGHT;

    public Label() {
        super(
                ColorAndText.DEFAULT, Unit.INSTANCE,
                ColorAndText.CODEC, MyCodecs.unit(Unit.INSTANCE),
                null, 0
        );
    }

    @Override
    public Vec2d getSize(ColorAndText s) {
        return new Vec2d(SCALE * ServerFontWidth.getWidth(s.text()), 1);
    }
}
