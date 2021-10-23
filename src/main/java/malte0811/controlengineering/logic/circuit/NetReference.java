package malte0811.controlengineering.logic.circuit;

import com.mojang.serialization.Codec;

import java.util.Objects;

public record NetReference(String id) {
    public static final Codec<NetReference> CODEC = Codec.STRING.xmap(NetReference::new, NetReference::id);
}
