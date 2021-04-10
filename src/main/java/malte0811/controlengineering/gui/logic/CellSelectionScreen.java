package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CellSelectionScreen extends StackedScreen {
    private static final float TEXT_SCALE = 4;
    private static final int BORDER_SIZE_X = 70;
    private static final int BORDER_SIZE_Y = 30;
    private static final int BACKGROUND_COLOR = 0xffdddddd;
    private static final int SELECTED_COLOR = 0xff77dd77;
    private final Consumer<SymbolInstance<?>> select;
    private final List<SchematicSymbol<?>> symbols;
    private int xGrid;
    private int yGrid;
    private int numCols;
    // Necessary to prevent closing two screens at once, which isn't possible
    private SymbolInstance<?> selected;

    public CellSelectionScreen(Consumer<SymbolInstance<?>> select) {
        super(new StringTextComponent("Cell selection"));
        this.select = select;
        this.symbols = new ArrayList<>(SchematicSymbols.REGISTRY.getValues());
    }

    @Override
    protected void init() {
        super.init();
        xGrid = symbols.stream()
                .mapToInt(symbol -> (int) Math.max(
                        symbol.getXSize(), font.getStringPropertyWidth(symbol.getName()) / TEXT_SCALE
                ))
                .max()
                .orElse(5) + 2;
        yGrid = symbols.stream()
                .mapToInt(SchematicSymbol::getYSize)
                .max()
                .orElse(5) + 2 + getTotalFontHeight();
        numCols = (width - 2 * BORDER_SIZE_X) / (xGrid * LogicDesignScreen.BASE_SCALE);
    }

    @Override
    protected void renderForeground(
            @Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {
        matrixStack.push();
        matrixStack.translate(BORDER_SIZE_X, BORDER_SIZE_Y, 0);
        matrixStack.scale(LogicDesignScreen.BASE_SCALE, LogicDesignScreen.BASE_SCALE, 1);
        int index = 0;
        for (int row = 0; index < symbols.size(); ++row) {
            for (int col = 0; index < symbols.size() && col < numCols; ++col) {
                SchematicSymbol<?> symbol = symbols.get(index);
                final int xBase = col * xGrid + (xGrid - symbol.getXSize()) / 2;
                final int yBase = row * yGrid + 1;
                symbol.render(matrixStack, xBase, yBase + getTotalFontHeight(), null);
                //TODO less push/pop's?
                matrixStack.push();
                matrixStack.translate(xBase, yBase, 0);
                matrixStack.scale(1 / TEXT_SCALE, 1 / TEXT_SCALE, 1);
                ITextComponent desc = symbol.getName();
                final int offset = (int) ((symbol.getXSize() * TEXT_SCALE - font.getStringPropertyWidth(desc)) / 2);
                font.drawText(matrixStack, desc, offset, 0, 0xff000000);
                matrixStack.pop();
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
        matrixStack.scale(LogicDesignScreen.BASE_SCALE, LogicDesignScreen.BASE_SCALE, 1);
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
            symbols.get(selected).createInstanceWithUI(i -> this.selected = i);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (selected != null) {
            select.accept(selected);
            closeScreen();
        }
    }

    private int getSelectedIndex(double mouseX, double mouseY) {
        final int col = (int) ((mouseX - BORDER_SIZE_X) / (xGrid * LogicDesignScreen.BASE_SCALE));
        final int row = (int) ((mouseY - BORDER_SIZE_Y) / (yGrid * LogicDesignScreen.BASE_SCALE));
        if (col < 0 || row < 0 || col >= numCols) {
            return -1;
        }
        final int index = row * numCols + col;
        if (index < symbols.size()) {
            return index;
        } else {
            return -1;
        }
    }

    private int getTotalFontHeight() {
        return (int) (Minecraft.getInstance().fontRenderer.FONT_HEIGHT / TEXT_SCALE + 1);
    }
}
