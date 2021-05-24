package malte0811.controlengineering.controlpanels.components.config;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.serialization.ListBasedCodec;
import malte0811.controlengineering.util.serialization.serial.BasicCodecParser;

import java.util.Objects;

public class ColorAndText {
    public static final Codec<ColorAndText> CODEC = ListBasedCodec.create(
            "color", BasicCodecParser.HEX_INT, ColorAndText::getColor,
            "text", Codec.STRING, ColorAndText::getText,
            ColorAndText::new
    );
    public static final ColorAndText DEFAULT = new ColorAndText(0, "Text");

    private final int color;
    private final String text;

    public ColorAndText(int color, String text) {
        this.color = color;
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorAndText config = (ColorAndText) o;
        return color == config.color && text.equals(config.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, text);
    }
}
