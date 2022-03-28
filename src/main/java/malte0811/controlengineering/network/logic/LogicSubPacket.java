package malte0811.controlengineering.network.logic;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class LogicSubPacket {
    static final List<Function<FriendlyByteBuf, ? extends LogicSubPacket>> FROM_BYTES = new ArrayList<>();
    static final Object2IntMap<Class<? extends LogicSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        register(FullSync.class, FullSync::new);
        register(AddSymbol.class, AddSymbol::new);
        register(AddWire.class, AddWire::new);
        register(Delete.class, Delete::new);
    }

    private static <T extends LogicSubPacket>
    void register(Class<T> type, Function<FriendlyByteBuf, T> construct) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(construct);
    }

    protected static LogicSubPacket read(FriendlyByteBuf buffer) {
        init();
        return FROM_BYTES.get(buffer.readVarInt()).apply(buffer);
    }

    public final void writeFull(FriendlyByteBuf buffer) {
        init();
        buffer.writeVarInt(BY_TYPE.getInt(getClass()));
        write(buffer);
    }

    protected abstract void write(FriendlyByteBuf out);

    public abstract boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level);

    public boolean allowSendingToServer() {
        return true;
    }
}
