package malte0811.controlengineering.network.logic;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class LogicSubPacket {
    private static final Logger LOGGER = LogManager.getLogger();
    static final List<Function<PacketBuffer, LogicSubPacket>> FROM_BYTES = new ArrayList<>();
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
    }

    private static <T extends LogicSubPacket>
    void register(Class<T> type, Function<PacketBuffer, LogicSubPacket> construct) {
        BY_TYPE.put(type, FROM_BYTES.size());
        FROM_BYTES.add(construct);
    }

    protected static LogicSubPacket read(PacketBuffer buffer) {
        init();
        return FROM_BYTES.get(buffer.readVarInt()).apply(buffer);
    }

    public final void writeFull(PacketBuffer buffer) {
        init();
        buffer.writeVarInt(BY_TYPE.getInt(getClass()));
        write(buffer);
    }

    protected abstract void write(PacketBuffer out);

    protected abstract void process(Schematic applyTo, Consumer<Schematic> replace);

    public boolean allowSendingToServer() {
        return true;
    }
}
