package malte0811.controlengineering.logic.cells;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class LeafcellType<State> {
    public static final String TYPE_KEY = "type";
    public static final String STATE_KEY = "type";
    private static final Map<ResourceLocation, LeafcellType<?>> TYPES = new ConcurrentHashMap<>();

    public static <T extends LeafcellType<?>> T register(T type) {
        Preconditions.checkState(!TYPES.containsKey(type.getName()), "Duplicate cell type: " + type.getName());
        TYPES.put(type.getName(), type);
        return type;
    }

    @Nullable
    public static LeafcellInstance<?> fromNBT(CompoundNBT nbt) {
        String typeName = nbt.getString(TYPE_KEY);
        ResourceLocation rl = ResourceLocation.tryCreate(typeName);
        if (rl == null) {
            return null;
        }
        LeafcellType<?> type = TYPES.get(rl);
        if (type == null) {
            return null;
        }
        return type.readInstance(nbt);
    }

    private final ResourceLocation name;
    private final List<Pin> inputPins;
    private final List<Pin> outputPins;
    private final State initialState;
    private final Codec<State> stateCodec;

    protected LeafcellType(
            ResourceLocation name,
            List<Pin> inputPins,
            List<Pin> outputPins,
            State initialState,
            Codec<State> stateCodec
    ) {
        this.name = name;
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.initialState = initialState;
        this.stateCodec = stateCodec;
    }

    public LeafcellInstance<State> newInstance() {
        return new LeafcellInstance<>(this, initialState);
    }

    public final ResourceLocation getName() {
        return name;
    }

    final CompoundNBT toNBT(State state) {
        INBT stateNbt = Codecs.encode(stateCodec, state);
        CompoundNBT result = new CompoundNBT();
        result.put(STATE_KEY, stateNbt);
        result.putString(STATE_KEY, getName().toString());
        return result;
    }

    public abstract State nextState(DoubleList inputSignals, State currentState);

    public abstract DoubleList getOutputSignals(DoubleList inputSignals, State currentState);

    protected static boolean bool(double value) {
        return value == 1;
    }

    @Nullable
    private LeafcellInstance<State> readInstance(CompoundNBT nbt) {
        return Codecs.read(stateCodec, nbt.get(STATE_KEY))
                .result()
                .map(s -> new LeafcellInstance<>(this, s))
                .orElse(null);
    }
}
