package malte0811.controlengineering.network.panellayout;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class PanelSubPacket {
    static final List<Function<FriendlyByteBuf, ? extends PanelSubPacket>> FROM_BYTES = new ArrayList<>();
    static final Object2IntMap<Class<? extends PanelSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        register(Delete.class, Delete::new);
        register(Add.class, Add::new);
        register(FullSync.class, FullSync::new);
        register(Replace.class, Replace::new);
        initialized = true;
    }

    private static <T extends PanelSubPacket>
    void register(Class<T> type, Function<FriendlyByteBuf, T> construct) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(construct);
    }

    protected static PanelSubPacket read(FriendlyByteBuf buffer) {
        init();
        return FROM_BYTES.get(buffer.readVarInt()).apply(buffer);
    }

    public final void writeFull(FriendlyByteBuf buffer) {
        init();
        buffer.writeVarInt(BY_TYPE.getInt(getClass()));
        write(buffer);
    }

    protected abstract void write(FriendlyByteBuf out);

    public abstract boolean process(Level level, List<PlacedComponent> allComponents);

    public boolean allowSendingToServer() {
        return true;
    }
}
