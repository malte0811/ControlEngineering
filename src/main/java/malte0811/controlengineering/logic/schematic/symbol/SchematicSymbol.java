package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.util.typereg.TypedRegistryEntry;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class SchematicSymbol<State> extends TypedRegistryEntry<State> {
    protected SchematicSymbol(State initialState, Codec<State> stateCodec) {
        super(initialState, stateCodec);
    }

    @Override
    public SymbolInstance<State> newInstance() {
        return new SymbolInstance<>(this, getInitialState());
    }

    public abstract void render(MatrixStack transform, int x, int y, @Nullable State state);

    public abstract int getXSize();

    public abstract int getYSize();

    public abstract List<SymbolPin> getInputPins();

    public abstract List<SymbolPin> getOutputPins();

    public abstract void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone);

    public abstract ITextComponent getDesc();
}
