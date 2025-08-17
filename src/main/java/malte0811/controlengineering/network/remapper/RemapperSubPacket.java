package malte0811.controlengineering.network.remapper;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public interface RemapperSubPacket {
    List<StreamCodec<? super FriendlyByteBuf, ? extends RemapperSubPacket>> CODECS = new ArrayList<>();
    Object2IntMap<Class<? extends RemapperSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    StreamCodec<FriendlyByteBuf, RemapperSubPacket> CODEC = ByteBufCodecs.VAR_INT
            .<FriendlyByteBuf>cast()
            .dispatch(p -> BY_TYPE.getInt(p.getClass()), CODECS::get);

    static void init() {
        register(FullSync.class, FullSync.CODEC);
        register(SetMapping.class, SetMapping.CODEC);
        register(ClearMapping.class, ClearMapping.CODEC);
    }

    private static <T extends RemapperSubPacket>
    void register(Class<T> type, StreamCodec<? super FriendlyByteBuf, ? extends RemapperSubPacket> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec);
    }

    int[] process(int[] colorToGray);

    default boolean allowSendingToServer() {
        return true;
    }
}
