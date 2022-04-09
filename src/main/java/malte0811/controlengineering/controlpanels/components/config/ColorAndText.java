package malte0811.controlengineering.controlpanels.components.config;

import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.mycodec.record.RecordCodecBase;

public record ColorAndText(int color, String text) {
    public static final RecordCodecBase<ColorAndText> CODEC = new RecordCodec2<>(
            new CodecField<>("color", ColorAndText::color, MyCodecs.HEX_COLOR),
            new CodecField<>("text", ColorAndText::text, MyCodecs.STRING),
            ColorAndText::new
    );
    public static final ColorAndText DEFAULT = new ColorAndText(0, "Text");
}
