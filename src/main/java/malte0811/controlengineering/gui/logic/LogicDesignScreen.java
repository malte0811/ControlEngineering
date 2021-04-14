package malte0811.controlengineering.gui.logic;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.logic.schematic.ConnectedPin;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.network.logic.*;
import malte0811.controlengineering.util.TextUtil;
import malte0811.controlengineering.util.Vec2d;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.util.math.MathHelper.ceil;
import static net.minecraft.util.math.MathHelper.floor;

public class LogicDesignScreen extends StackedScreen implements IHasContainer<LogicDesignContainer> {
    public static final String COMPONENTS_KEY = ControlEngineering.MODID + ".gui.components";
    public static final String ENABLE_DRC_KEY = ControlEngineering.MODID + ".gui.drcOn";
    public static final String DISABLE_DRC_KEY = ControlEngineering.MODID + ".gui.drcOff";

    private static final int TRANSLUCENT_BORDER_SIZE = 20;
    private static final int WHITE_BORDER_SIZE = 1;
    private static final int TOTAL_BORDER = TRANSLUCENT_BORDER_SIZE + WHITE_BORDER_SIZE;
    public static final int BASE_SCALE = 3;

    // TODO store schematic in there?
    private final LogicDesignContainer container;
    private Schematic schematic;
    @Nullable
    private Vec2i currentWireStart = null;
    @Nullable
    private SymbolInstance<?> placingSymbol = null;
    private List<ConnectedPin> errors = ImmutableList.of();
    private boolean errorsShown = false;
    private float currentScale = BASE_SCALE;
    private double centerX = 0;
    private double centerY = 0;

    public LogicDesignScreen(LogicDesignContainer container, ITextComponent title) {
        super(title);
        this.schematic = new Schematic();
        this.container = container;
    }

    @Override
    protected void init() {
        super.init();
        addButton(new Button(
                TOTAL_BORDER, TOTAL_BORDER, 20, 20, new StringTextComponent("C"),
                btn -> minecraft.displayGuiScreen(new CellSelectionScreen(s -> placingSymbol = s)),
                makeTooltip(() -> COMPONENTS_KEY)
        ));
        addButton(new Button(
                TOTAL_BORDER, TOTAL_BORDER + 20, 20, 20, new StringTextComponent("E"),
                btn -> {
                    errorsShown = !errorsShown;
                    updateErrors();
                },
                makeTooltip(() -> errorsShown ? DISABLE_DRC_KEY : ENABLE_DRC_KEY)
        ));
    }

    private Button.ITooltip makeTooltip(Supplier<String> key) {
        return ($, transform, x, y) -> this.renderTooltip(transform, new TranslationTextComponent(key.get()), x, y);
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
        drawErrors(matrixStack);
        Vec2d mousePos = getMousePosition(mouseX, mouseY);
        schematic.render(matrixStack, mousePos);
        PlacedSymbol placed = getPlacingSymbol(mousePos);
        if (placed != null) {
            placed.render(matrixStack);
        }
        WireSegment placedWire = getPlacingSegment(mousePos);
        if (placedWire != null) {
            final int color = schematic.canAdd(placedWire) ? 0xff785515 : 0xffff5515;
            placedWire.renderWithoutBlobs(matrixStack, color);
        }
        RenderSystem.disableScissor();

        matrixStack.pop();
        PlacedSymbol hovered = schematic.getSymbolAt(mousePos);
        if (hovered != null) {
            ITextComponent toShow = hovered.getSymbol().getName();
            List<IFormattableTextComponent> extra = hovered.getSymbol().getExtraDesc();
            if (extra.isEmpty()) {
                renderTooltip(matrixStack, toShow, mouseX, mouseY);
            } else {
                List<IReorderingProcessor> tooltip = new ArrayList<>(1 + extra.size());
                tooltip.add(toShow.func_241878_f());
                for (IFormattableTextComponent extraLine : extra) {
                    TextUtil.addTooltipLineReordering(tooltip, extraLine);
                }
                renderTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }
    }

    private void drawErrors(MatrixStack transform) {
        for (ConnectedPin pin : errors) {
            final Vec2i pos = pin.getPosition();
            fill(transform, pos.x - 1, pos.y - 1, pos.x + 2, pos.y + 2, 0xffff0000);
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
        final Vec2d mousePos = getMousePosition(mouseX, mouseY);
        PlacedSymbol placed = getPlacingSymbol(mousePos);
        if (placed != null) {
            if (schematic.canPlace(placed)) {
                schematic.addSymbol(placed);
                sendToServer(new AddSymbol(placed));
            }
        } else {
            WireSegment placedWire = getPlacingSegment(mousePos);
            if (placedWire != null) {
                if (schematic.canAdd(placedWire)) {
                    schematic.addWire(placedWire);
                    sendToServer(new AddWire(placedWire));
                    if (placedWire.getEnd().equals(currentWireStart)) {
                        currentWireStart = placedWire.getStart();
                    } else {
                        currentWireStart = placedWire.getEnd();
                    }
                }
            } else {
                currentWireStart = mousePos.floor();
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
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (currentWireStart != null || placingSymbol != null) {
                currentWireStart = null;
                placingSymbol = null;
                return true;
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            MouseHelper helper = Minecraft.getInstance().mouseHelper;
            MainWindow window = Minecraft.getInstance().getMainWindow();
            final Vec2d mousePos = getMousePosition(
                    helper.getMouseX() * window.getScaledWidth() / (double) window.getWidth(),
                    helper.getMouseY() * window.getScaledHeight() / (double) window.getHeight()
            );
            if (schematic.removeOneContaining(mousePos)) {
                sendToServer(new Delete(mousePos));
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

    private Vec2d getMousePosition(double mouseX, double mouseY) {
        return new Vec2d(
                (mouseX - width / 2. - centerX) / currentScale,
                (mouseY - height / 2. - centerY) / currentScale
        );
    }

    @Nullable
    private PlacedSymbol getPlacingSymbol(Vec2d pos) {
        if (placingSymbol != null) {
            return new PlacedSymbol(pos.floor(), placingSymbol);
        } else {
            return null;
        }
    }

    @Nullable
    private WireSegment getPlacingSegment(Vec2d pos) {
        if (currentWireStart != null) {
            final double sizeX = Math.abs(pos.x - currentWireStart.x - .5);
            final double sizeY = Math.abs(pos.y - currentWireStart.y - .5);
            if (sizeX > sizeY) {
                return new WireSegment(
                        new Vec2i(getWireStart(currentWireStart.x, pos.x), currentWireStart.y),
                        getWireLength(currentWireStart.x, pos.x),
                        WireSegment.WireAxis.X
                );
            } else {
                return new WireSegment(
                        new Vec2i(currentWireStart.x, getWireStart(currentWireStart.y, pos.y)),
                        getWireLength(currentWireStart.y, pos.y),
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

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    private void sendToServer(LogicSubPacket data) {
        ControlEngineering.NETWORK.sendToServer(new LogicPacket(data));
        updateErrors();
    }

    @Nonnull
    @Override
    public LogicDesignContainer getContainer() {
        return container;
    }

    public void updateErrors() {
        if (errorsShown) {
            errors = schematic.toCircuit().right().orElse(ImmutableList.of());
        }
    }
}
