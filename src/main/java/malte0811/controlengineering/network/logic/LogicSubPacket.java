package malte0811.controlengineering.network.logic;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class LogicSubPacket {
    static final List<MyCodec<? extends LogicSubPacket>> CODECS = new ArrayList<>();
    static final Object2IntMap<Class<? extends LogicSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        register(FullSync.class, FullSync.CODEC);
        register(AddSymbol.class, AddSymbol.CODEC);
        register(AddWire.class, AddWire.CODEC);
        register(Delete.class, Delete.CODEC);
        register(ClearAll.class, ClearAll.CODEC);
        register(SetName.class, SetName.CODEC);
        register(ModifySymbol.class, ModifySymbol.CODEC);
    }

    private static <T extends LogicSubPacket>
    void register(Class<T> type, MyCodec<T> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec);
    }

    protected static LogicSubPacket read(FriendlyByteBuf buffer) {
        init();
        return CODECS.get(buffer.readVarInt()).from(buffer);
    }

    public final void writeFull(FriendlyByteBuf buffer) {
        init();
        final var index = BY_TYPE.getInt(getClass());
        buffer.writeVarInt(index);
        CODECS.get(index).toSerialUnchecked(new PacketBufferStorage(buffer), this);
    }

    public abstract boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level);

    public boolean allowSendingToServer() {
        return true;
    }

    public boolean canApplyOnReadOnly() {
        return false;
    }
}
