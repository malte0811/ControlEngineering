package malte0811.controlengineering.network.remapper;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record FullSync(int[] colorToGray) implements RemapperSubPacket {
    public static final StreamCodec<ByteBuf, FullSync> CODEC = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list())
            .map(
                    l -> new FullSync(l.stream().mapToInt(i -> i).toArray()),
                    p -> Arrays.stream(p.colorToGray).boxed().toList()
            );

    @Override
    public int[] process(int[] colorToGray) {
        return this.colorToGray;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
