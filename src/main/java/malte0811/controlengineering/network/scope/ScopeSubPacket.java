package malte0811.controlengineering.network.scope;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.blockentity.bus.ScopeBlockEntity.ModuleInScope;
import malte0811.controlengineering.gui.scope.ScopeMenu;
import malte0811.controlengineering.scope.GlobalConfig;
import malte0811.controlengineering.scope.GlobalState;
import malte0811.controlengineering.scope.module.ScopeModuleInstance;
import malte0811.controlengineering.scope.trace.Traces;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.mutable.Mutable;

import java.util.ArrayList;
import java.util.List;

public final class ScopeSubPacket {
    static final List<StreamCodec<? super FriendlyByteBuf, ? extends IScopeSubPacket>> CODECS = new ArrayList<>();
    static final Object2IntMap<Class<? extends IScopeSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    public static final StreamCodec<FriendlyByteBuf, IScopeSubPacket> CODEC = ByteBufCodecs.VAR_INT
            .<FriendlyByteBuf>cast()
            .dispatch(p -> BY_TYPE.getInt(p.getClass()), CODECS::get);

    public static void init() {
        register(FullSync.class, FullSync.CODEC);
        register(ModuleConfig.class, ModuleConfig.CODEC);
        register(AddTraceSamples.class, AddTraceSamples.CODEC);
        register(InitTraces.class, InitTraces.CODEC);
        register(SetGlobalCfg.class, SetGlobalCfg.CODEC);
        register(ResetSweep.class, ResetSweep.CODEC);
        register(SetGlobalState.class, SetGlobalState.CODEC);
    }

    private static <T extends IScopeSubPacket>
    void register(Class<T> type, MyCodec<T> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec.streamCodec());
    }

    public static boolean processFull(IScopeSubPacket packet, ScopeMenu menu) {
        if (!packet.process(
                menu.getModules(), menu.getTracesMutable(), menu.getGlobalConfigMutable(), menu.getGlobalStateMutable()
        )) {
            return false;
        }
        ScopeModuleInstance.ensureOneTriggerActive(menu.getModules(), -1);
        return true;
    }

    public interface IScopeSubPacket {
        boolean process(
                List<ModuleInScope> modules,
                Mutable<Traces> traces,
                Mutable<GlobalConfig> globalConfig,
                Mutable<GlobalState> globalState
        );

        default boolean allowSendingToServer() {
            return true;
        }
    }
}
