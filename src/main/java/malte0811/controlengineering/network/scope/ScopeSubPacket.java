package malte0811.controlengineering.network.scope;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.scope.ScopeModuleInstance;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class ScopeSubPacket {
    static final List<MyCodec<? extends IScopeSubPacket>> CODECS = new ArrayList<>();
    static final Object2IntMap<Class<? extends IScopeSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        register(SyncModules.class, SyncModules.CODEC);
        register(ModuleConfig.class, ModuleConfig.CODEC);
    }

    private static <T extends IScopeSubPacket>
    void register(Class<T> type, MyCodec<T> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec);
    }

    static IScopeSubPacket read(FriendlyByteBuf buffer) {
        init();
        return CODECS.get(buffer.readVarInt()).from(buffer);
    }

    public static boolean processFull(IScopeSubPacket packet, List<ScopeModuleInstance<?>> modules) {
        if (!packet.process(modules)) { return false; }
        ScopeModuleInstance.ensureOneTriggerActive(modules);
        return true;
    }

    public interface IScopeSubPacket {
        boolean process(List<ScopeModuleInstance<?>> modules);

        default void writeFull(FriendlyByteBuf buffer) {
            init();
            final var index = BY_TYPE.getInt(getClass());
            buffer.writeVarInt(index);
            CODECS.get(index).toSerialUnchecked(new PacketBufferStorage(buffer), this);
        }

        default boolean allowSendingToServer() {
            return true;
        }
    }
}
