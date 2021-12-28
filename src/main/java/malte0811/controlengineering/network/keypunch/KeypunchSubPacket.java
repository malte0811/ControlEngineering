package malte0811.controlengineering.network.keypunch;

import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class KeypunchSubPacket {
    static final List<Function<FriendlyByteBuf, ? extends KeypunchSubPacket>> FROM_BYTES = new ArrayList<>();
    static final Object2IntMap<Class<? extends KeypunchSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        register(FullSync.class, FullSync::new);
        register(TypeChar.class, TypeChar::new);
        register(Backspace.class, Backspace::new);
        initialized = true;
    }

    private static <T extends KeypunchSubPacket>
    void register(Class<T> type, Function<FriendlyByteBuf, T> construct) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(construct);
    }

    protected static KeypunchSubPacket read(FriendlyByteBuf buffer) {
        init();
        return FROM_BYTES.get(buffer.readVarInt()).apply(buffer);
    }

    public final void writeFull(FriendlyByteBuf buffer) {
        init();
        buffer.writeVarInt(BY_TYPE.getInt(getClass()));
        write(buffer);
    }

    protected abstract void write(FriendlyByteBuf out);

    public abstract boolean process(KeypunchState state);

    public void process(ByteConsumer remotePrint) {}

    public boolean allowSendingToServer() {
        return true;
    }
}
