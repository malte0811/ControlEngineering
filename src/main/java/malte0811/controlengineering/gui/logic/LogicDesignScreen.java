package malte0811.controlengineering.gui.logic;

import blusunrize.lib.manual.ManualUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.logic.CircuitIngredientDrawer.BigItemStack;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity.AvailableIngredients;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.misc.ConfirmScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.widget.SmallCheckbox;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.ConnectedPin;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.network.logic.*;
import malte0811.controlengineering.util.ScreenUtils;
import malte0811.controlengineering.util.TextUtil;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.CallbackI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import static net.minecraft.util.Mth.ceil;
import static net.minecraft.util.Mth.floor;

record CopiedRegion(List<PlacedSymbol> symbols, List<WireSegment> segments) {
}

class SelectionManager {

    private double startX = -1;
    private double startY = -1;
    private double endX = -1;
    private double endY = -1;

    private Schematic schematic;

    public SelectionManager(Schematic schematic) {
        this.schematic = schematic;
    }

    public void renderSelection(PoseStack matrixStack) {
        if (startX == -1 || startY == -1 || endX == -1 || endY == -1) {
            return;
        }

        matrixStack.pushPose();

        ScreenUtils.fill(
                matrixStack,
                startX, startY,
                endX, endY,
                0x20_ff_00_00
        );

        matrixStack.popPose();
    }

    public void startSelection(double mouseX, double mouseY) {
        this.startX = mouseX;
        this.startY = mouseY;
    }

    public void endSelection(double mouseX, double mouseY) {
        this.endX = mouseX;
        this.endY = mouseY;
    }

    public void clearSelection() {
        this.startX = -1;
        this.startY = -1;
        this.endX = -1;
        this.endY = -1;
    }

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    /**
     * Takes two consumers first one runs on every symbol in selection, the second one runs on every wireSegment in selection
     *
     * @param runOnSymbols consumer to be run on every symbol
     * @param runOnWires   consumer to be run on every segment
     */
    public void runOnComponentsInSelection(
            BiConsumer<PlacedSymbol, RectangleI> runOnSymbols,
            BiConsumer<WireSegment, RectangleI> runOnWires
    ) {
        if (startX == -1 || startY == -1 || endX == -1 || endY == -1) {
            return;
        }

        var rectangle = getSelection();

        schematic.getSymbolsInRectangle(rectangle)
                .forEach(symbol -> runOnSymbols.accept(symbol, rectangle));

        schematic.getNets().stream()
                .flatMap(net -> Streams.stream(net.getAllSegments()))
                .filter(wireSegment -> rectangle.containsClosed(wireSegment.start()) || rectangle.containsClosed(wireSegment.end()))
                .forEach(segment -> runOnWires.accept(segment, rectangle));
    }

    public RectangleI getSelection() {
        if (startX == -1 || startY == -1 || endX == -1 || endY == -1) {
            return null;
        }
        return new RectangleI((int) startX, (int) startY, (int) endX, (int) endY);
    }
}

public class LogicDesignScreen extends StackedScreen implements MenuAccess<LogicDesignMenu> {
    private static final String KEY_PREFIX = ControlEngineering.MODID + ".logicworkbench.";
    public static final String COMPONENTS_KEY = KEY_PREFIX + "components";
    public static final String COMPONENTS_TOOLTIP = KEY_PREFIX + "components.tooltip";

    public static final String SET_NAME_KEY = KEY_PREFIX + "setName";
    public static final String SET_NAME_TOOLTIP = KEY_PREFIX + "setName.tooltip";
    public static final String SET_NAME_MESSAGE = KEY_PREFIX + "setName.message";

    public static final String CLEAR_ALL_KEY = KEY_PREFIX + "clearAll";
    public static final String CLEAR_ALL_TOOLTIP = KEY_PREFIX + "clearAll.tooltip";
    public static final String CLEAR_ALL_MESSAGE = KEY_PREFIX + "clearAll.warning";

    public static final String DRC_INFO_KEY = KEY_PREFIX + "drcOn";
    public static final String DIGITAL_PIN_KEY = KEY_PREFIX + "digitalPin";
    public static final String ANALOG_PIN_KEY = KEY_PREFIX + "analogPin";

    private static final int TRANSLUCENT_BORDER_SIZE = 20;
    private static final int WHITE_BORDER_SIZE = 1;
    private static final int TOTAL_BORDER = TRANSLUCENT_BORDER_SIZE + WHITE_BORDER_SIZE;
    public static final int BASE_SCALE = 3;

    private final LogicDesignMenu container;
    private Schematic schematic;
    @Nullable
    private Vec2i currentWireStart = null;
    @Nullable
    private PlacingSymbol placingSymbol = null;
    private boolean resetAfterPlacingSymbol = false;
    private List<ConnectedPin> errors = ImmutableList.of();
    private boolean errorsShown = false;
    private float minScale = 0.5F;
    private float currentScale = BASE_SCALE;
    // In schematic coordinates
    private double centerX = 0;
    private double centerY = 0;

    private boolean isCtrlPressed = false;
    private final SelectionManager selectionManager;

    private CopiedRegion copiedRegion;

    public LogicDesignScreen(LogicDesignMenu container, Component title) {
        super(title);
        this.schematic = new Schematic();
        this.container = container;
        this.selectionManager = new SelectionManager(schematic);
    }

    @Override
    protected void init() {
        super.init();
        if (!container.readOnly) {
            addRenderableWidget(new Button(
                    TOTAL_BORDER, TOTAL_BORDER, 40, 20, new TranslatableComponent(COMPONENTS_KEY),
                    btn -> minecraft.setScreen(new CellSelectionScreen(s -> {
                        placingSymbol = new PlacingSymbol(s, Vec2d.ZERO);
                        resetAfterPlacingSymbol = false;
                    })),
                    makeTooltip(COMPONENTS_TOOLTIP)
            ));
            addRenderableWidget(new Button(
                    TOTAL_BORDER, TOTAL_BORDER + 20, 40, 20, new TranslatableComponent(SET_NAME_KEY),
                    this::handleSetName, makeTooltip(SET_NAME_TOOLTIP)
            ));
            addRenderableWidget(new Button(
                    TOTAL_BORDER, TOTAL_BORDER + 40, 40, 20, new TranslatableComponent(CLEAR_ALL_KEY),
                    this::handleClearAll, makeTooltip(CLEAR_ALL_TOOLTIP)
            ));
            addRenderableWidget(new SmallCheckbox(
                    TOTAL_BORDER, TOTAL_BORDER + 60, 20, 20, new TextComponent("DRC"), errorsShown,
                    newState -> {
                        errorsShown = newState;
                        updateErrors();
                    },
                    makeTooltip(DRC_INFO_KEY)
            ));
        }
        minScale = Math.max(
                getScaleForShownSize(width, Schematic.BOUNDARY.getWidth()),
                getScaleForShownSize(height, Schematic.BOUNDARY.getHeight())
        );
    }

    private void handleSetName(Button $) {
        Minecraft.getInstance().setScreen(DataProviderScreen.makeFor(
                new TranslatableComponent(SET_NAME_MESSAGE),
                schematic.getName(),
                MyCodecs.STRING,
                s -> runAndSendToServer(new SetName(s))
        ));
    }

    private void handleClearAll(Button $) {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                new TranslatableComponent(CLEAR_ALL_MESSAGE), () -> runAndSendToServer(new ClearAll())
        ));
    }

    private Button.OnTooltip makeTooltip(String key) {
        return ($, transform, x, y) -> this.renderTooltip(transform, new TranslatableComponent(key), x, y);
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        drawCenteredString(matrixStack, minecraft.font, schematic.getName(), width / 2, TOTAL_BORDER - 11, -1);
        matrixStack.translate(width / 2., height / 2., 0);
        matrixStack.scale(currentScale, currentScale, 1);
        matrixStack.translate(-centerX, -centerY, 0);

        final double scale = minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor(
                (int) (TOTAL_BORDER * scale), (int) (TOTAL_BORDER * scale),
                (int) ((width - 2 * TOTAL_BORDER) * scale), (int) ((height - 2 * TOTAL_BORDER) * scale)
        );
        drawErrors(matrixStack);
        drawBoundary(matrixStack);
        Vec2d mousePos = getMousePosition(mouseX, mouseY);
        ClientSymbols.render(schematic, matrixStack, mousePos);

        Optional<Component> currentError = Optional.empty();
        PlacedSymbol placed = getPlacingSymbol(mousePos);
        if (placed != null) {
            currentError = schematic.makeChecker(minecraft.level).getErrorForAdding(placed);
            ClientSymbols.render(placed, matrixStack);
        } else {
            WireSegment placedWire = getPlacingSegment(mousePos);
            if (placedWire != null) {
                currentError = schematic.makeChecker(minecraft.level).getErrorForAdding(placedWire);
                final int color = currentError.isPresent() ? 0xffff5515 : 0xff785515;
                placedWire.renderWithoutBlobs(matrixStack, color);
            }
        }
        selectionManager.renderSelection(matrixStack);

        renderCopiedRegion(matrixStack, mousePos);
        RenderSystem.disableScissor();

        matrixStack.popPose();
        renderIngredients(matrixStack);
        renderTooltip(matrixStack, mouseX, mouseY, mousePos, currentError.orElse(null));
    }

    private void renderCopiedRegion(PoseStack matrixStack, Vec2d mousePos) {
        if (copiedRegion == null) {
            return;
        }
        copiedRegion.symbols().stream().map(symbol -> new PlacedSymbol(symbol.position().add((int) mousePos.x(), (int) mousePos.y()), symbol.symbol()))
                .forEach(symbol -> ClientSymbols.render(symbol, matrixStack));
        copiedRegion.segments().stream().map(segment -> new WireSegment(segment.start().add((int) mousePos.x(), (int) mousePos.y()), segment.length(), segment.axis()))
                .forEach(wireSegment -> wireSegment.renderWithoutBlobs(matrixStack, 0xffff5515));
    }

    private void renderTooltip(
            PoseStack transform, int mouseX, int mouseY, Vec2d schematicMouse, @Nullable Component currentError
    ) {
        if (currentError != null) {
            if (currentError instanceof MutableComponent mutable) {
                mutable.setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
            }
            renderTooltip(transform, currentError, mouseX, mouseY);
        } else {
            PlacedSymbol hovered = schematic.getSymbolAt(schematicMouse, minecraft.level);
            if (hovered != null) {
                Component toShow = hovered.symbol().getName();
                List<FormattedCharSequence> tooltip = new ArrayList<>();
                tooltip.add(toShow.getVisualOrderText());
                for (SymbolPin pin : getHoveredPins(hovered, schematicMouse)) {
                    var key = pin.type() == SignalType.DIGITAL ? DIGITAL_PIN_KEY : ANALOG_PIN_KEY;
                    tooltip.add(
                            new TranslatableComponent(key, pin.pinName())
                                    .withStyle(ChatFormatting.GRAY)
                                    .getVisualOrderText()
                    );
                }
                List<MutableComponent> extra = hovered.symbol().getExtraDesc();
                for (MutableComponent extraLine : extra) {
                    TextUtil.addTooltipLineReordering(tooltip, extraLine);
                }
                renderTooltip(transform, tooltip, mouseX, mouseY);
            }
        }
    }

    private void renderIngredients(PoseStack transform) {
        AvailableIngredients stored = container.getAvailableIngredients();
        transform.pushPose();
        transform.translate(width - TOTAL_BORDER - 17, height - TOTAL_BORDER - 17, 0);
        final int numTubes = schematic.getNumLogicTubes();
        final int numWires = schematic.getWireLength();
        final int numBoards = LogicCabinetBlockEntity.getNumBoardsFor(numTubes);
        renderIngredient(transform, null, numBoards, IEItemRefs.CIRCUIT_BOARD);
        transform.translate(0, -16, 0);
        renderIngredient(
                transform, stored != null ? stored.getAvailableTubes() : null, numTubes, IEItemRefs.TUBE
        );
        transform.translate(0, -16, 0);
        //TODO fix default
        renderIngredient(
                transform, stored != null ? stored.getAvailableWires() : null, numWires, IEItemRefs.WIRE
        );
        transform.popPose();
    }

    private void renderIngredient(
            PoseStack transform, @Nullable BigItemStack available, int required, ItemLike defaultItem
    ) {
        MutableComponent info;
        if (available != null) {
            info = new TextComponent(Math.min(available.count(), required) + " / " + required);
            if (available.count() < required) {
                info.withStyle(ChatFormatting.RED);
            }
        } else {
            info = new TextComponent(Integer.toString(required));
        }
        info.append(" x ");
        final Font font = Minecraft.getInstance().font;
        final int width = font.width(info);
        font.draw(transform, info, -width, (16 - font.lineHeight) / 2f, -1);
        ItemStack renderType;
        if (available == null || available.type().isEmpty()) {
            renderType = defaultItem.asItem().getDefaultInstance();
        } else {
            renderType = available.type();
        }
        ManualUtils.renderItemStack(transform, renderType, 0, 0, false);
    }

    private List<SymbolPin> getHoveredPins(PlacedSymbol hovered, Vec2d schematicMouse) {
        List<SymbolPin> result = new ArrayList<>();
        for (SymbolPin pin : hovered.symbol().getPins()) {
            if (new ConnectedPin(hovered, pin).getShape().containsClosed(schematicMouse)) {
                result.add(pin);
            }
        }
        return result;
    }

    private void drawErrors(PoseStack transform) {
        for (ConnectedPin pin : errors) {
            final Vec2i pos = pin.getPosition();
            fill(transform, pos.x() - 1, pos.y() - 1, pos.x() + 2, pos.y() + 2, 0xffff0000);
        }
    }

    private void drawBoundary(PoseStack transform) {
        final int color = 0xff_ff_dd_dd;
        final float offset = 2 / currentScale;
        ScreenUtils.fill(
                transform,
                Schematic.GLOBAL_MIN - offset, Schematic.GLOBAL_MIN - offset,
                Schematic.GLOBAL_MAX + offset, Schematic.GLOBAL_MIN,
                color
        );
        ScreenUtils.fill(
                transform,
                Schematic.GLOBAL_MIN - offset, Schematic.GLOBAL_MIN - offset,
                Schematic.GLOBAL_MIN, Schematic.GLOBAL_MAX + offset,
                color
        );
        ScreenUtils.fill(
                transform,
                Schematic.GLOBAL_MAX, Schematic.GLOBAL_MIN - offset,
                Schematic.GLOBAL_MAX + offset, Schematic.GLOBAL_MAX + offset,
                color
        );
        ScreenUtils.fill(
                transform,
                Schematic.GLOBAL_MIN - offset, Schematic.GLOBAL_MAX,
                Schematic.GLOBAL_MAX + offset, Schematic.GLOBAL_MAX + offset,
                color
        );
    }

    private double mouseXDown;
    private double mouseYDown;
    // Start at true: We don't want to consider a release if there wasn't a click before it
    private boolean clickWasConsumed = true;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        selectionManager.clearSelection();
        if (isCtrlPressed) {
            double x = (mouseX - width / 2.) / currentScale + centerX;
            double y = (mouseY - height / 2.) / currentScale + centerY;
            selectionManager.startSelection(x, y);
        } else {
            mouseXDown = mouseX;
            mouseYDown = mouseY;
            clickWasConsumed = false;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (isCtrlPressed && copiedRegion == null) {
            double x = (mouseX - width / 2.) / currentScale + centerX;
            double y = (mouseY - height / 2.) / currentScale + centerY;
            selectionManager.endSelection(x, y);
            return true;
        }
        if (clickWasConsumed) {
            return false;
        }
        clickWasConsumed = true;

        if (copiedRegion != null) {
            pasteCopiedRegion(copiedRegion);

            if (!isCtrlPressed)
                copiedRegion = null;

            return true;
        }

        final Vec2d mousePos = getMousePosition((int) mouseXDown, (int) mouseYDown);
        PlacedSymbol placed = getPlacingSymbol(mousePos);
        if (placed != null) {
            if (schematic.makeChecker(minecraft.level).canAdd(placed)) {
                runAndSendToServer(new AddSymbol(placed));
                if (resetAfterPlacingSymbol) {
                    placingSymbol = null;
                }
            }
        } else {
            WireSegment placedWire = getPlacingSegment(mousePos);
            if (placedWire != null) {
                if (schematic.makeChecker(minecraft.level).canAdd(placedWire)) {
                    runAndSendToServer(new AddWire(placedWire));
                    if (placedWire.end().equals(currentWireStart)) {
                        currentWireStart = placedWire.start();
                    } else {
                        currentWireStart = placedWire.end();
                    }
                }
            } else {
                var clicked = schematic.getSymbolAt(mousePos, minecraft.level);
                if (clicked != null) {
                    handleSymbolClick(clicked, clicked.symbol(), mousePos, button);
                } else if (!container.readOnly) {
                    currentWireStart = mousePos.floor();
                }
            }
        }
        return true;
    }

    private void pasteCopiedRegion(CopiedRegion region) {
        final Vec2i mousePos = getMousePosition(ScreenUtils.getMousePosition()).floor();

        region.symbols()
                .stream()
                .map(symbol -> new PlacedSymbol(
                        symbol.position().add(mousePos),
                        symbol.symbol())
                )
                .filter(symbol -> schematic.makeChecker(minecraft.level).canAdd(symbol))
                .forEach(symbol -> runAndSendToServer(new AddSymbol(symbol)));

        region.segments()
                .stream()
                .map(segment -> new WireSegment(
                        segment.start().add(mousePos),
                        segment.length(),
                        segment.axis())
                )
                .filter(segment -> schematic.makeChecker(minecraft.level).canAdd(segment))
                .forEach(segment -> runAndSendToServer(new AddWire(segment)));
    }

    private <State>
    void handleSymbolClick(PlacedSymbol clicked, SymbolInstance<State> instance, Vec2d mousePos, int button) {
        if (!getHoveredPins(clicked, mousePos).isEmpty() && !container.readOnly) {
            currentWireStart = mousePos.floor();
            return;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (runAndSendToServer(new Delete(mousePos))) {
                placingSymbol = new PlacingSymbol(
                        clicked.symbol(), clicked.position().subtract(mousePos).add(0.5, 0.5)
                );
                resetAfterPlacingSymbol = true;
            }
        } else if (!container.readOnly || instance.getType().canConfigureOnReadOnly()) {
            ClientSymbols.createInstanceWithUI(
                    instance.getType(),
                    newInst -> runAndSendToServer(new ModifySymbol(new PlacedSymbol(clicked.position(), newInst))),
                    instance.getCurrentState()
            );
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (!clickWasConsumed && !(Math.abs(mouseX - mouseXDown) > 1) && !(Math.abs(mouseY - mouseYDown) > 1)) {
            return false;
        }
        if (isCtrlPressed) {
            return true;
        }
        centerX -= dragX / currentScale;
        centerY -= dragY / currentScale;
        clampView();
        clickWasConsumed = true;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            final float zoomScale = 1.1f;
            if (delta > 0) {
                setScale(currentScale * zoomScale);
            } else {
                setScale(currentScale / zoomScale);
            }
            clampView();
        }
        return true;
    }

    private void setScale(float newScale) {
        currentScale = Mth.clamp(newScale, minScale, 10);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!container.readOnly) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (currentWireStart != null || placingSymbol != null) {
                    currentWireStart = null;
                    placingSymbol = null;
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                final Vec2d mousePos = getMousePosition(ScreenUtils.getMousePosition());
                if (schematic.removeOneContaining(mousePos, minecraft.level)) {
                    sendToServer(new Delete(mousePos));
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
                isCtrlPressed = true;
            } else if (keyCode == GLFW.GLFW_KEY_C && modifiers == GLFW.GLFW_MOD_CONTROL) {
                this.copiedRegion = copySelection();
            } else if (keyCode == GLFW.GLFW_KEY_V && modifiers == GLFW.GLFW_MOD_CONTROL && copiedRegion != null) {
                pasteCopiedRegion(copiedRegion);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public CopiedRegion copySelection() {
        List<PlacedSymbol> symbols = new ArrayList<>();
        List<WireSegment> segments = new ArrayList<>();

        selectionManager.runOnComponentsInSelection(
                (symbol, rectangle) -> symbols.add(new PlacedSymbol(symbol.position().subtract(rectangle.min()), symbol.symbol())),
                (segment, rectangle) -> segments.add(new WireSegment(segment.start().subtract(rectangle.min()), segment.length(), segment.axis()))
        );

        return new CopiedRegion(symbols, segments);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!container.readOnly) {
            if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) {
                isCtrlPressed = false;
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderCustomBackground(
            @Nonnull PoseStack matrixStack,
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

    private Vec2d getMousePosition(Vec2d screenPos) {
        return getMousePosition(screenPos.x(), screenPos.y());
    }

    private Vec2d getMousePosition(double mouseX, double mouseY) {
        return new Vec2d(
                (mouseX - width / 2.) / currentScale + centerX,
                (mouseY - height / 2.) / currentScale + centerY
        );
    }

    @Nullable
    private PlacedSymbol getPlacingSymbol(Vec2d pos) {
        if (placingSymbol != null) {
            return new PlacedSymbol(pos.add(placingSymbol.offsetToMouse()).floor(), placingSymbol.symbol());
        } else {
            return null;
        }
    }

    @Nullable
    private WireSegment getPlacingSegment(Vec2d pos) {
        if (currentWireStart != null) {
            final double sizeX = Math.abs(pos.x() - currentWireStart.x() - .5);
            final double sizeY = Math.abs(pos.y() - currentWireStart.y() - .5);
            if (sizeX > sizeY) {
                return new WireSegment(
                        new Vec2i(getWireStart(currentWireStart.x(), pos.x()), currentWireStart.y()),
                        getWireLength(currentWireStart.x(), pos.x()),
                        WireSegment.WireAxis.X
                );
            } else {
                return new WireSegment(
                        new Vec2i(currentWireStart.x(), getWireStart(currentWireStart.y(), pos.y())),
                        getWireLength(currentWireStart.y(), pos.y()),
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
        selectionManager.setSchematic(schematic);
        var level = Objects.requireNonNull(Minecraft.getInstance().level);
        RectangleI totalArea = null;
        for (var symbol : schematic.getSymbols()) {
            totalArea = symbol.getShape(level).union(totalArea);
        }
        for (var net : schematic.getNets()) {
            for (var wire : net.getAllSegments()) {
                totalArea = wire.getShape().union(totalArea);
            }
        }
        if (totalArea == null) {
            totalArea = new RectangleI(-1, -1, 1, 1);
        }
        var center = totalArea.center();
        centerX = center.x();
        centerY = center.y();
        // Subtract 3x border to get a bit of space between circuit and actual border
        var scaleX = (width - 3 * TOTAL_BORDER) / (float) totalArea.getWidth();
        var scaleY = (height - 3 * TOTAL_BORDER) / (float) totalArea.getHeight();
        setScale(Math.min(Math.min(scaleX, scaleY), BASE_SCALE));
        clampView();
    }

    public Schematic getSchematic() {
        return schematic;
    }

    private boolean runAndSendToServer(LogicSubPacket data) {
        if (!data.canApplyOnReadOnly() && container.readOnly) {
            return false;
        }
        if (process(data)) {
            sendToServer(data);
            return true;
        }
        return false;
    }

    public boolean process(LogicSubPacket packet) {
        if (packet.process(getSchematic(), this::setSchematic, Minecraft.getInstance().level)) {
            updateErrors();
            return true;
        }
        return false;
    }

    private void sendToServer(LogicSubPacket data) {
        ControlEngineering.NETWORK.sendToServer(new LogicPacket(data));
        updateErrors();
    }

    @Nonnull
    @Override
    public LogicDesignMenu getMenu() {
        return container;
    }

    public void updateErrors() {
        if (errorsShown) {
            errors = SchematicCircuitConverter.getFloatingInputs(schematic);
        } else {
            errors = ImmutableList.of();
        }
    }

    private void clampView() {
        final double halfScreenWidth = getShownSizeForScale(width, currentScale) / 2;
        final double halfScreenHeight = getShownSizeForScale(height, currentScale) / 2;
        centerX = Mth.clamp(
                centerX, Schematic.GLOBAL_MIN + halfScreenWidth, Schematic.GLOBAL_MAX - halfScreenWidth
        );
        centerY = Mth.clamp(
                centerY, Schematic.GLOBAL_MIN + halfScreenHeight, Schematic.GLOBAL_MAX - halfScreenHeight
        );
    }

    private static float getShownSizeForScale(float dimensionSize, float scale) {
        return (dimensionSize - 2 * TOTAL_BORDER - 5) / scale;
    }

    private static float getScaleForShownSize(float size, float shownSize) {
        return (size - 2 * TOTAL_BORDER - 5) / shownSize;
    }

    private record PlacingSymbol(SymbolInstance<?> symbol, Vec2d offsetToMouse) {
    }
}
