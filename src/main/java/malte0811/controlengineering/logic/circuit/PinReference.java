package malte0811.controlengineering.logic.circuit;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;

public record PinReference(int cell, boolean isOutput, String pinName) {
    public static final MyCodec<PinReference> CODEC = new RecordCodec3<>(
            new CodecField<>("cell", PinReference::cell, MyCodecs.INTEGER),
            new CodecField<>("isOut", PinReference::isOutput, MyCodecs.BOOL),
            new CodecField<>("pin", PinReference::pinName, MyCodecs.STRING),
            PinReference::new
    );
}
