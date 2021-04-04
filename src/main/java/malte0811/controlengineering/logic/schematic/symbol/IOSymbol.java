package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.gui.bus.BusSignalSelector;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.function.Consumer;

public class IOSymbol extends SchematicSymbol<BusSignalRef> {
    private final boolean isInput;

    public IOSymbol(boolean isInput) {
        super(new BusSignalRef(0, 0), BusSignalRef.CODEC);
        this.isInput = isInput;
    }

    @Override
    public void render(MatrixStack transform, int x, int y, BusSignalRef state) {

    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public List<Vec2i> getInputPins() {
        return ImmutableList.of();
    }

    @Override
    public List<Vec2i> getOutputPins() {
        return ImmutableList.of();
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<BusSignalRef>> onDone) {
        Minecraft.getInstance().displayGuiScreen(new BusSignalSelector(ref -> {
            SymbolInstance<BusSignalRef> instance = new SymbolInstance<>(this, ref);
            onDone.accept(instance);
        }));
    }
}
