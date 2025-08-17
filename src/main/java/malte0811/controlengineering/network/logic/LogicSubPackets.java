package malte0811.controlengineering.network.logic;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class LogicSubPackets {
    static final List<StreamCodec<? super FriendlyByteBuf, ? extends LogicSubPacket>> CODECS = new ArrayList<>();
    static final Object2IntMap<Class<? extends LogicSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    public static final StreamCodec<FriendlyByteBuf, LogicSubPacket> CODEC = ByteBufCodecs.VAR_INT
            .<FriendlyByteBuf>cast()
            .dispatch(p -> BY_TYPE.getInt(p.getClass()), CODECS::get);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        register(FullSync.class, FullSync.CODEC);
        register(Add.class, Add.CODEC);
        register(Delete.class, Delete.CODEC);
        register(DeleteArea.class, DeleteArea.CODEC);
        register(ClearAll.class, ClearAll.CODEC);
        register(SetName.class, SetName.CODEC);
        register(ModifySymbol.class, ModifySymbol.CODEC);
    }

    private static <T extends LogicSubPacket>
    void register(Class<T> type, MyCodec<T> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec.streamCodec());
    }

    public abstract boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level);

    public boolean allowSendingToServer() {
        return true;
    }

    public boolean canApplyOnReadOnly() {
        return false;
    }
}
