package malte0811.controlengineering.network.keypunch;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public interface KeypunchSubPacket {
    boolean process(KeypunchState state);

    default void process(ByteConsumer remotePrint) { }

    default boolean allowSendingToServer() {
        return true;
    }
}
