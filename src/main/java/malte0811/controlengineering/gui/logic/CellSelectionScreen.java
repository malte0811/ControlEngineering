package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.widget.PageSelector;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbols;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final int numCols = 4;
    private final int numRowsPerPage = 4;
    private PageSelector pageSelector;
    // Necessary to prevent closing two screens at once, which isn't possible
    private SymbolInstance<?> selected;

    public CellSelectionScreen(Consumer<SymbolInstance<?>> select) {
        super(new TextComponent("Cell selection"));
        this.select = select;
        this.symbols = new ArrayList<>(SchematicSymbols.REGISTRY.getValues());
    }

    @Override
    protected void init() {
        super.init();
        xGrid = (width - 2 * BORDER_SIZE_X) / (numCols * LogicDesignScreen.BASE_SCALE);
        yGrid = (height - 2 * BORDER_SIZE_Y - PageSelector.HEIGHT) / (numRowsPerPage * LogicDesignScreen.BASE_SCALE);
        this.pageSelector = addRenderableWidget(new PageSelector(
                BORDER_SIZE_X, height - BORDER_SIZE_Y - PageSelector.HEIGHT, width - 2 * BORDER_SIZE_X,
                Mth.positiveCeilDiv(symbols.size(), numCols * numRowsPerPage),
                this.pageSelector != null ? this.pageSelector.getCurrentPage() : 0
        ));
    }

    @Override
    protected void renderForeground(
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {
        matrixStack.pushPose();
        matrixStack.translate(BORDER_SIZE_X, BORDER_SIZE_Y, 0);
        matrixStack.scale(LogicDesignScreen.BASE_SCALE, LogicDesignScreen.BASE_SCALE, 1);
        final var fontHeight = getTotalFontHeight();
        int index = getFirstIndexOnPage();
        for (int row = 0; index < symbols.size() && row < numRowsPerPage; ++row) {
            for (int col = 0; index < symbols.size() && col < numCols; ++col) {
                SchematicSymbol<?> symbol = symbols.get(index);
                final int xBase = col * xGrid;
                final int yBase = row * yGrid;
                ClientSymbols.renderCenteredInBox(
                        symbol.newInstance(), matrixStack, xBase, yBase + fontHeight, xGrid, yGrid - fontHeight - 1
                );
                matrixStack.pushPose();
                matrixStack.translate(xBase + xGrid / 2., yBase + 1, 0);
                matrixStack.scale(1 / TEXT_SCALE, 1 / TEXT_SCALE, 1);
                Component desc = symbol.getName();
                final var offset = -font.width(desc) / 2f;
                font.draw(matrixStack, desc, offset, 0, 0xff000000);
                matrixStack.popPose();
                ++index;
            }
        }
        matrixStack.popPose();
    }

    @Override
    protected void renderCustomBackground(
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks
    ) {
        super.renderCustomBackground(matrixStack, mouseX, mouseY, partialTicks);
        final int borderRenderSize = 5;
        fill(
                matrixStack,
                BORDER_SIZE_X - borderRenderSize,
                BORDER_SIZE_Y - borderRenderSize,
                this.width - BORDER_SIZE_X + borderRenderSize,
                this.height - BORDER_SIZE_Y - PageSelector.HEIGHT,
                BACKGROUND_COLOR
        );
        matrixStack.pushPose();
        matrixStack.translate(BORDER_SIZE_X, BORDER_SIZE_Y, 0);
        matrixStack.scale(LogicDesignScreen.BASE_SCALE, LogicDesignScreen.BASE_SCALE, 1);
        final int selected = getSelectedIndex(mouseX, mouseY) - getFirstIndexOnPage();
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
        matrixStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        final int selected = getSelectedIndex(mouseX, mouseY);
        if (selected >= 0) {
            ClientSymbols.createInstanceWithUI(symbols.get(selected), i -> this.selected = i);
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
            onClose();
        }
    }

    private int getSelectedIndex(double mouseX, double mouseY) {
        final int col = (int) ((mouseX - BORDER_SIZE_X) / (xGrid * LogicDesignScreen.BASE_SCALE));
        final int row = (int) ((mouseY - BORDER_SIZE_Y) / (yGrid * LogicDesignScreen.BASE_SCALE));
        if (col < 0 || row < 0 || col >= numCols || row >= numRowsPerPage) {
            return -1;
        }
        final int index = row * numCols + col + getFirstIndexOnPage();
        if (index < symbols.size()) {
            return index;
        } else {
            return -1;
        }
    }

    private int getFirstIndexOnPage() {
        return this.pageSelector.getCurrentPage() * numRowsPerPage * numCols;
    }

    private int getTotalFontHeight() {
        return (int) (Minecraft.getInstance().font.lineHeight / TEXT_SCALE + 1);
    }

    private static Level level() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }
}
