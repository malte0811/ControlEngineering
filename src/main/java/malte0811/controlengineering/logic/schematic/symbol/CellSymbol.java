package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.cells.LeafcellType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols.SYMBOLS_SHEET;

public class CellSymbol extends SchematicSymbol<Unit> {
    private final LeafcellType<?> type;
    private final SubTexture texture;
    private final List<SymbolPin> inputPins;
    private final List<SymbolPin> outputPins;

    public CellSymbol(
            LeafcellType<?> type,
            int uMin, int vMin, int uSize, int vSize,
            List<SymbolPin> inputPins, List<SymbolPin> outputPins
    ) {
        super(Unit.INSTANCE, Codec.unit(Unit.INSTANCE));
        this.type = type;
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.texture = new SubTexture(SYMBOLS_SHEET, uMin, vMin, uMin + uSize, vMin + vSize);
    }

    @Override
    public void render(MatrixStack transform, int x, int y, @Nullable Unit state) {
        texture.blit(transform, x, y);
    }

    @Override
    public int getXSize() {
        return texture.getWidth();
    }

    @Override
    public int getYSize() {
        return texture.getHeight();
    }

    @Override
    public List<SymbolPin> getInputPins() {
        return inputPins;
    }

    @Override
    public List<SymbolPin> getOutputPins() {
        return outputPins;
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<Unit>> onDone) {
        // No config required/possible
        onDone.accept(newInstance());
    }

    public static String getTranslationKey(LeafcellType<?> type) {
        return "cell." + type.getRegistryName().getNamespace() + "." + type.getRegistryName().getPath() + ".name";
    }

    @Override
    public ITextComponent getDesc() {
        return new TranslationTextComponent(getTranslationKey(type));
    }
}
