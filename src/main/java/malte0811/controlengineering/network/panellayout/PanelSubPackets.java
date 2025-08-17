package malte0811.controlengineering.network.panellayout;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class PanelSubPackets {
    static final List<StreamCodec<? super FriendlyByteBuf, ? extends PanelSubPacket>> CODECS = new ArrayList<>();
    static final Object2IntMap<Class<? extends PanelSubPacket>> BY_TYPE = new Object2IntOpenHashMap<>();
    public static final StreamCodec<FriendlyByteBuf, PanelSubPacket> CODEC = ByteBufCodecs.VAR_INT
            .<FriendlyByteBuf>cast()
            .dispatch(p -> BY_TYPE.getInt(p.getClass()), CODECS::get);
    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        register(Delete.class, Delete.CODEC);
        register(Add.class, Add.CODEC);
        register(FullSync.class, FullSync.CODEC);
        register(Replace.class, Replace.CODEC);
        initialized = true;
    }

    private static <T extends PanelSubPacket>
    void register(Class<T> type, StreamCodec<? super FriendlyByteBuf, T> codec) {
        BY_TYPE.put(type, CODECS.size());
        CODECS.add(codec);
    }
}
