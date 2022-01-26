package malte0811.controlengineering.logic.circuit;

import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;

public record NetReference(String id) {
    public static final MyCodec<NetReference> CODEC = MyCodecs.STRING.xmap(NetReference::new, NetReference::id);
}
