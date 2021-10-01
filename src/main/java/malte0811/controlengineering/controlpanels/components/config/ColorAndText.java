package malte0811.controlengineering.controlpanels.components.config;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.serialization.ListBasedCodec;
import malte0811.controlengineering.util.serialization.serial.BasicCodecParser;

import java.util.Objects;

public record ColorAndText(int color, String text) {
    public static final Codec<ColorAndText> CODEC = ListBasedCodec.create(
            "color", BasicCodecParser.HEX_INT, ColorAndText::color,
            "text", Codec.STRING, ColorAndText::text,
            ColorAndText::new
    );
    public static final ColorAndText DEFAULT = new ColorAndText(0, "Text");
}
