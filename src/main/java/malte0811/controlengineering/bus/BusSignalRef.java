package malte0811.controlengineering.bus;

import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;
import malte0811.controlengineering.util.mycodec.record.RecordCodecBase;

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

    public int index() {
        return line * BusLine.LINE_SIZE + color;
    }

    public static BusSignalRef fromIndex(int index) {
        if (index < 0 || index >= BusLine.LINE_SIZE * BusWireType.NUM_LINES) {
            return null;
        } else {
            return new BusSignalRef(index / BusLine.LINE_SIZE, index % BusLine.LINE_SIZE);
        }
    }
}
