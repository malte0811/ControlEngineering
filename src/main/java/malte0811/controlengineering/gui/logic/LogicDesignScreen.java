package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraft.util.math.MathHelper.ceil;
import static net.minecraft.util.math.MathHelper.floor;

public class LogicDesignScreen extends StackedScreen {
    private static final int TRANSLUCENT_BORDER_SIZE = 20;
    private static final int WHITE_BORDER_SIZE = 1;
    private static final int TOTAL_BORDER = TRANSLUCENT_BORDER_SIZE + WHITE_BORDER_SIZE;
    public static final int BASE_SCALE = 3;

    //TODO this needs to be synced and saved in the tile
    private final Schematic schematic = new Schematic();
    @Nullable
    private Vec2i currentWireStart = null;
    @Nullable
    private SymbolInstance<?> placingSymbol = null;
    private float currentScale = BASE_SCALE;
    private double centerX = 0;
    private double centerY = 0;

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
        matrixStack.push();
        matrixStack.translate(width / 2. + centerX, height / 2. + centerY, 0);
        matrixStack.scale(currentScale, currentScale, 1);

        final double scale = minecraft.getMainWindow().getGuiScaleFactor();
        RenderSystem.enableScissor(
                (int) (TOTAL_BORDER * scale), (int) (TOTAL_BORDER * scale),
                (int) ((width - 2 * TOTAL_BORDER) * scale), (int) ((height - 2 * TOTAL_BORDER) * scale)
        );
        final double actualMouseX = getMousePosition(mouseX, centerX, width);
        final double actualMouseY = getMousePosition(mouseY, centerY, height);
        schematic.render(matrixStack);
        PlacedSymbol placed = getPlacingSymbol(actualMouseX, actualMouseY);
        if (placed != null) {
            placed.render(matrixStack);
        }
        WireSegment placedWire = getPlacingSegment(actualMouseX, actualMouseY);
        if (placedWire != null) {
            placedWire.renderWithoutBlobs(matrixStack, 0xff785515);
        }
        RenderSystem.disableScissor();

        matrixStack.pop();
        PlacedSymbol hovered = schematic.getSymbolAt(actualMouseX, actualMouseY);
        if (hovered != null) {
            ITextComponent toShow = hovered.getSymbol().getDesc();
            renderTooltip(matrixStack, toShow, mouseX, mouseY);
        }
    }

    private double mouseXDown;
    private double mouseYDown;
    // Start at true: We don't want to consider a release if there wasn't a click before it
    private boolean clickConsumedAsDrag = true;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        mouseXDown = mouseX;
        mouseYDown = mouseY;
        clickConsumedAsDrag = false;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (clickConsumedAsDrag) {
            return false;
        }
        clickConsumedAsDrag = true;
        final double posX = getMousePosition(mouseX, centerX, width);
        final double posY = getMousePosition(mouseY, centerY, height);
        PlacedSymbol placed = getPlacingSymbol(posX, posY);
        if (placed != null) {
            if (schematic.canPlace(placed)) {
                schematic.addSymbol(placed);
            }
        } else {
            WireSegment placedWire = getPlacingSegment(posX, posY);
            if (placedWire != null) {
                schematic.addWire(placedWire);
                if (placedWire.getEnd().equals(currentWireStart)) {
                    currentWireStart = placedWire.getStart();
                } else {
                    currentWireStart = placedWire.getEnd();
                }
            } else {
                currentWireStart = new Vec2i(floor(posX), floor(posY));
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (!clickConsumedAsDrag && !(Math.abs(mouseX - mouseXDown) > 1) && !(Math.abs(mouseY - mouseYDown) > 1)) {
            return false;
        }
        centerX += dragX;
        centerY += dragY;
        clickConsumedAsDrag = true;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            final float zoomScale = 1.1f;
            if (delta > 0) {
                currentScale *= zoomScale;
            } else {
                currentScale /= zoomScale;
            }
            currentScale = MathHelper.clamp(currentScale, 0.5F, 10);
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

    private double getMousePosition(double basePosition, double axisCenter, double screenSize) {
        return (basePosition - screenSize / 2 - axisCenter) / currentScale;
    }

    @Nullable
    private PlacedSymbol getPlacingSymbol(double posX, double posY) {
        if (placingSymbol != null) {
            return new PlacedSymbol((int) posX, (int) posY, placingSymbol);
        } else {
            return null;
        }
    }

    @Nullable
    private WireSegment getPlacingSegment(double posX, double posY) {
        if (currentWireStart != null) {
            final double sizeX = Math.abs(posX - currentWireStart.x - .5);
            final double sizeY = Math.abs(posY - currentWireStart.y - .5);
            if (sizeX > sizeY) {
                return new WireSegment(
                        new Vec2i(getWireStart(currentWireStart.x, posX), currentWireStart.y),
                        getWireLength(currentWireStart.x, posX),
                        WireSegment.WireAxis.X
                );
            } else {
                return new WireSegment(
                        new Vec2i(currentWireStart.x, getWireStart(currentWireStart.y, posY)),
                        getWireLength(currentWireStart.y, posY),
                        WireSegment.WireAxis.Y
                );
            }
        }
        return null;
    }

    private int getWireStart(int fixed, double mouse) {
        return mouse < fixed ? floor(mouse) : fixed;
    }

    private int getWireLength(int fixed, double mouse) {
        return mouse < fixed ? ceil(fixed - mouse) : floor(mouse - fixed);
    }
}
