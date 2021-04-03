package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.logic.schematic.SchematicSymbol;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static malte0811.controlengineering.logic.schematic.CellSymbols.SELECTABLE_SYMBOLS;

public class CellSelectionScreen extends StackedScreen {
    private static final int BORDER_SIZE_X = 70;
    private static final int BORDER_SIZE_Y = 30;
    private static final int BACKGROUND_COLOR = 0xffdddddd;
    private static final int SELECTED_COLOR = 0xff77dd77;
    private final Consumer<SchematicSymbol> select;
    private int xGrid;
    private int yGrid;
    private int numCols;

    public CellSelectionScreen(Consumer<SchematicSymbol> select) {
        super(new StringTextComponent("Cell selection"));
        this.select = select;
    }

    @Override
    protected void init() {
        super.init();
        xGrid = SELECTABLE_SYMBOLS.stream()
                .mapToInt(SchematicSymbol::getXSize)
                .max()
                .orElse(5) + 2;
        yGrid = SELECTABLE_SYMBOLS.stream()
                .mapToInt(SchematicSymbol::getYSize)
                .max()
                .orElse(5) + 2;
        numCols = (int) ((width - 2 * BORDER_SIZE_X) / (xGrid * SchematicSymbol.BASE_SCALE));
    }

    @Override
    protected void renderForeground(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {
        matrixStack.push();
        matrixStack.translate(BORDER_SIZE_X, BORDER_SIZE_Y, 0);
        matrixStack.scale(SchematicSymbol.BASE_SCALE, SchematicSymbol.BASE_SCALE, 1);
        int index = 0;
        for (int row = 0; index < SELECTABLE_SYMBOLS.size(); ++row) {
            for (int col = 0; index < SELECTABLE_SYMBOLS.size() && col < numCols; ++col) {
                SchematicSymbol symbol = SELECTABLE_SYMBOLS.get(index);
                symbol.render(matrixStack, col * xGrid + (xGrid - symbol.getXSize()) / 2, row * yGrid);
                ++index;
            }
        }
        matrixStack.pop();
    }

    @Override
    protected void renderCustomBackground(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {
        super.renderCustomBackground(matrixStack, mouseX, mouseY, partialTicks);
        final int borderRenderSize = 5;
        fill(
                matrixStack,
                BORDER_SIZE_X - borderRenderSize,
                BORDER_SIZE_Y - borderRenderSize,
                this.width - BORDER_SIZE_X + borderRenderSize,
                this.height - BORDER_SIZE_Y + borderRenderSize,
                BACKGROUND_COLOR
        );
        matrixStack.push();
        matrixStack.translate(BORDER_SIZE_X, BORDER_SIZE_Y, 0);
        matrixStack.scale(SchematicSymbol.BASE_SCALE, SchematicSymbol.BASE_SCALE, 1);
        final int selected = getSelectedIndex(mouseX, mouseY);
        if (selected >= 0) {
            final int row = selected / numCols;
            final int col = selected % numCols;
            fill(
                    matrixStack,
                    col * xGrid,
                    row * yGrid,
                    col * xGrid + xGrid,
                    row * yGrid + yGrid,
                    SELECTED_COLOR
            );
        }
        matrixStack.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        final int selected = getSelectedIndex(mouseX, mouseY);
        if (selected >= 0) {
            select.accept(SELECTABLE_SYMBOLS.get(selected));
            closeScreen();
            return true;
        } else {
            return false;
        }
    }

    private int getSelectedIndex(double mouseX, double mouseY) {
        final int col = (int) ((mouseX - BORDER_SIZE_X) / (xGrid * SchematicSymbol.BASE_SCALE));
        final int row = (int) ((mouseY - BORDER_SIZE_Y) / (yGrid * SchematicSymbol.BASE_SCALE));
        if (col < 0 || row < 0 || col >= numCols) {
            return -1;
        }
        final int index = row * numCols + col;
        if (index < SELECTABLE_SYMBOLS.size()) {
            return index;
        } else {
            return -1;
        }
    }
}
