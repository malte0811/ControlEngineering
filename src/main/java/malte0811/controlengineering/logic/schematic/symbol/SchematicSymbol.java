package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class SchematicSymbol<State> extends TypedRegistryEntry<State, SymbolInstance<State>> {
    protected SchematicSymbol(State initialState, MyCodec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    @Override
    public SymbolInstance<State> newInstance(State state) {
        return new SymbolInstance<>(this, state);
    }

    public abstract int getXSize(State state, @Nonnull Level level);

    public final int getDefaultXSize(Level level) {
        return getXSize(getInitialState(), level);
    }

    public abstract int getYSize(State state, @Nonnull Level level);

    public final int getDefaultYSize(Level level) {
        return getYSize(getInitialState(), level);
    }

    public abstract List<SymbolPin> getPins(@Nullable State state);

    public abstract Component getName();

    public List<MutableComponent> getExtraDescription(State state) {
        return ImmutableList.of();
    }
}
