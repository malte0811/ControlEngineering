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
    private final List<SymbolPin> pins;

    public CellSymbol(LeafcellType<?> type, int uMin, int vMin, int uSize, int vSize, List<SymbolPin> pins) {
        super(Unit.INSTANCE, Codec.unit(Unit.INSTANCE));
        this.type = type;
        this.pins = pins;
        this.texture = new SubTexture(SYMBOLS_SHEET, uMin, vMin, uMin + uSize, vMin + vSize);
    }

    @Override
    public void renderCustom(MatrixStack transform, int x, int y, @Nullable Unit state) {
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
    public List<SymbolPin> getPins(@Nullable Unit unit) {
        return pins;
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
    public ITextComponent getName() {
        return new TranslationTextComponent(getTranslationKey(type));
    }

    public LeafcellType<?> getCellType() {
        return type;
    }
}
