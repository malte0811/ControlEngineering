package malte0811.controlengineering.network.remapper;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class RemapperSubPacket {
    static final List<Function<FriendlyByteBuf, ? extends RemapperSubPacket>> FROM_BYTES = new ArrayList<>();
    static final Object2IntMap<Class<? extends RemapperSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        register(FullSync.class, FullSync::new);
        register(SetMapping.class, SetMapping::new);
        register(ClearMapping.class, ClearMapping::new);
    }

    private static <T extends RemapperSubPacket>
    void register(Class<T> type, Function<FriendlyByteBuf, T> construct) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(construct);
    }

    protected static RemapperSubPacket read(FriendlyByteBuf buffer) {
        init();
        return FROM_BYTES.get(buffer.readVarInt()).apply(buffer);
    }

    public final void writeFull(FriendlyByteBuf buffer) {
        init();
        buffer.writeVarInt(BY_TYPE.getInt(getClass()));
        write(buffer);
    }

    protected abstract void write(FriendlyByteBuf out);

    protected abstract int[] process(int[] colorToGray);

    public boolean allowSendingToServer() {
        return true;
    }
}
