package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.logic.schematic.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.WireSymbol;
import malte0811.controlengineering.logic.schematic.WireSymbol.Axis;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.util.math.MathHelper.ceil;
import static net.minecraft.util.math.MathHelper.floor;

public class LogicDesignScreen extends StackedScreen {
    private static final int TRANSLUCENT_BORDER_SIZE = 20;
    private static final int WHITE_BORDER_SIZE = 1;
    private static final int TOTAL_BORDER = TRANSLUCENT_BORDER_SIZE + WHITE_BORDER_SIZE;

    //TODO this needs to be synced and saved in the tile
    private final Schematic schematic = new Schematic();
    @Nullable
    private Pair<Integer, Integer> currentWireStart = null;
    @Nullable
    private SchematicSymbol placingSymbol = null;

    public LogicDesignScreen() {
        super(new StringTextComponent("Logic Design"));
    }

    @Override
    protected void init() {
        super.init();
        addButton(new Button(
                TOTAL_BORDER, TOTAL_BORDER, 18, 18, new StringTextComponent("C"),
                btn -> minecraft.displayGuiScreen(new CellSelectionScreen(s -> placingSymbol = s))
        ));
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        double actualMouseX = getMousePosition(mouseX);
        double actualMouseY = getMousePosition(mouseY);
        matrixStack.push();
        matrixStack.translate(TOTAL_BORDER, TOTAL_BORDER, 0);
        matrixStack.scale(SchematicSymbol.BASE_SCALE, SchematicSymbol.BASE_SCALE, 1);
        schematic.render(matrixStack);
        PlacedSymbol placed = getPlacingSymbol(actualMouseX, actualMouseY);
        if (placed != null) {
            placed.render(matrixStack);
        }
        matrixStack.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        final double posX = getMousePosition(mouseX);
        final double posY = getMousePosition(mouseY);
        PlacedSymbol placed = getPlacingSymbol(posX, posY);
        if (placed != null) {
            if (schematic.canPlace(placed)) {
                schematic.addSymbol(placed);
                currentWireStart = null;
                //TODO continue wire?
            }
        } else {
            currentWireStart = Pair.of((int) posX, (int) posY);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (currentWireStart != null || placingSymbol != null) {
                currentWireStart = null;
                placingSymbol = null;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderCustomBackground(
            @Nonnull MatrixStack matrixStack,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        super.renderCustomBackground(matrixStack, mouseX, mouseY, partialTicks);
        fill(
                matrixStack,
                TRANSLUCENT_BORDER_SIZE,
                TRANSLUCENT_BORDER_SIZE,
                this.width - TRANSLUCENT_BORDER_SIZE,
                this.height - TRANSLUCENT_BORDER_SIZE,
                -1
        );
        fill(
                matrixStack,
                TOTAL_BORDER,
                TOTAL_BORDER,
                this.width - TOTAL_BORDER,
                this.height - TOTAL_BORDER,
                0xff3362ac
        );
    }

    private double getMousePosition(double basePosition) {
        return (basePosition - TOTAL_BORDER) / SchematicSymbol.BASE_SCALE;
    }

    @Nullable
    private PlacedSymbol getPlacingSymbol(double posX, double posY) {
        if (currentWireStart != null) {
            final double sizeX = Math.abs(posX - currentWireStart.getFirst() - .5);
            final double sizeY = Math.abs(posY - currentWireStart.getSecond() - .5);
            if (sizeX > sizeY) {
                return new PlacedSymbol(
                        getWireStart(currentWireStart.getFirst(), posX),
                        currentWireStart.getSecond(),
                        new WireSymbol(Axis.X, getWireLength(currentWireStart.getFirst(), posX))
                );
            } else {
                return new PlacedSymbol(
                        currentWireStart.getFirst(),
                        getWireStart(currentWireStart.getSecond(), posY),
                        new WireSymbol(Axis.Y, getWireLength(currentWireStart.getSecond(), posY))
                );
            }
        } else if (placingSymbol != null) {
            return new PlacedSymbol((int) posX, (int) posY, placingSymbol);
        } else {
            return null;
        }
    }

    private int getWireStart(int fixed, double mouse) {
        return mouse < fixed ? floor(mouse) : fixed;
    }

    private int getWireLength(int fixed, double mouse) {
        return mouse < fixed ? ceil(fixed - mouse) + 1 : floor(mouse - fixed);
    }
}
