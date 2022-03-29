package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.crafting.noncrafting.ServerFontRecipe;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.world.level.Level;

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
    public Vec2d getSize(ColorAndText s, Level level) {
        return new Vec2d(SCALE * ServerFontRecipe.getWidth(level, s.text()), 1);
    }
}
