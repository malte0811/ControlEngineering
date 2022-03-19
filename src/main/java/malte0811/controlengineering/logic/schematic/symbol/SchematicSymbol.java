package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.util.serialization.mycodec.MyCodec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class SchematicSymbol<State> extends TypedRegistryEntry<State, SymbolInstance<State>> {
    protected SchematicSymbol(State initialState, MyCodec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    @Override
    public SymbolInstance<State> newInstance(State state) {
        return new SymbolInstance<>(this, state);
    }

    protected abstract void renderCustom(PoseStack transform, int x, int y, @Nullable State state);

    public abstract int getXSize();

    public abstract int getYSize();

    public abstract List<SymbolPin> getPins(@Nullable State state);

    public abstract void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone, State initialState);

    public void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone) {
        createInstanceWithUI(onDone, getInitialState());
    }

    public abstract Component getName();

    public List<MutableComponent> getExtraDescription(State state) {
        return ImmutableList.of();
    }

    public final void render(PoseStack stack, int x, int y, @Nullable State state) {
        renderCustom(stack, x, y, state);
        for (SymbolPin pin : getPins(state)) {
            pin.render(stack, x, y, SchematicNet.WIRE_COLOR);
        }
    }
}
