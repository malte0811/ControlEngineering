package malte0811.controlengineering.network.keypunch;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class KeypunchSubpackets {
    private static final List<StreamCodec<? super FriendlyByteBuf, ? extends KeypunchSubPacket>> FROM_BYTES = new ArrayList<>();
    private static final Object2IntMap<Class<? extends KeypunchSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;
    public static final StreamCodec<FriendlyByteBuf, KeypunchSubPacket> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .<FriendlyByteBuf>cast()
            .dispatch(p -> BY_TYPE.getInt(p.getClass()), FROM_BYTES::get);

    public static void init() {
        if (initialized) {
            return;
        }
        register(FullSync.class, FullSync.CODEC);
        register(TypeChar.class, TypeChar.CODEC);
        register(Backspace.class, Backspace.CODEC);
        initialized = true;
    }

    private static <T extends KeypunchSubPacket>
    void register(Class<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(codec);
    }
}
