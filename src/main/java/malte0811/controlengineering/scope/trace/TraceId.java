package malte0811.controlengineering.scope.trace;

import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.RecordCodec2;

public record TraceId(int firstSlotOfModule, int traceIdWithinModule) {
    public static final MyCodec<TraceId> CODEC = new RecordCodec2<>(
            MyCodecs.INTEGER.fieldOf("firstSlotOfModule", TraceId::firstSlotOfModule),
            MyCodecs.INTEGER.fieldOf("traceIdWithinModule", TraceId::traceIdWithinModule),
            TraceId::new
    );
}
