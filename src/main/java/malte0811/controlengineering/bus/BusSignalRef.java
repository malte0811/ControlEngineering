package malte0811.controlengineering.bus;

import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import malte0811.controlengineering.util.serialization.mycodec.record.CodecField;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.serialization.mycodec.record.RecordCodecBase;

public record BusSignalRef(int line, int color) {
    public static final RecordCodecBase<BusSignalRef> CODEC = new RecordCodec2<>(
            new CodecField<>("line", BusSignalRef::line, MyCodecs.INTEGER),
            new CodecField<>("color", BusSignalRef::color, MyCodecs.INTEGER),
            BusSignalRef::new
    );
    public static final BusSignalRef DEFAULT = new BusSignalRef(0, 0);

    public BusState singleSignalState(int value) {
        return BusState.EMPTY.with(this, value);
    }
}
