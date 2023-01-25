package malte0811.controlengineering.gui.logic;

import blusunrize.lib.manual.ManualUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.logic.CircuitIngredientDrawer.BigItemStack;
import malte0811.controlengineering.blockentity.logic.LogicCabinetBlockEntity;
import malte0811.controlengineering.blockentity.logic.LogicWorkbenchBlockEntity.AvailableIngredients;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.misc.ConfirmScreen;
import malte0811.controlengineering.gui.misc.DataProviderScreen;
import malte0811.controlengineering.gui.widget.SmallCheckbox;
import malte0811.controlengineering.items.IEItemRefs;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.ConnectedPin;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.logic.schematic.symbol.SymbolPin;
import malte0811.controlengineering.network.logic.*;
import malte0811.controlengineering.util.TextUtil;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

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
    static final int TOTAL_BORDER = TRANSLUCENT_BORDER_SIZE + WHITE_BORDER_SIZE;

    private final LogicDesignMenu container;
    private Schematic schematic;
    private final PlacementHandler placementHandler;
    private final SchematicViewArea visibleArea;
    private List<ConnectedPin> errors = ImmutableList.of();
    private boolean errorsShown = false;

    private double mouseXDown;
    private double mouseYDown;
    // Start at consumed: We don't want to consider a release if there wasn't a click before it
    private ClickState clickState = ClickState.CLICKED_CONSUMED;

    public LogicDesignScreen(LogicDesignMenu container, Component title) {
        super(title);
        this.schematic = new Schematic();
        this.container = container;
        placementHandler = new PlacementHandler(minecraft, this, container.readOnly);
        visibleArea = new SchematicViewArea(minecraft);
    }

    @Override
    protected void init() {
        super.init();
        if (!container.readOnly) {
            addRenderableWidget(Button.builder(
                            Component.translatable(COMPONENTS_KEY),
                            btn -> minecraft.setScreen(new CellSelectionScreen(placementHandler::setPlacingSymbol))
                    )
                    .pos(TOTAL_BORDER, TOTAL_BORDER)
                    .size(40, 20)
                    .tooltip(Tooltip.create(Component.translatable(COMPONENTS_TOOLTIP)))
                    .build());
            addRenderableWidget(Button.builder(Component.translatable(SET_NAME_KEY), this::handleSetName)
                    .pos(TOTAL_BORDER, TOTAL_BORDER + 20)
                    .size(40, 20)
                    .tooltip(Tooltip.create(Component.translatable(SET_NAME_TOOLTIP)))
                    .build());
            addRenderableWidget(Button.builder(Component.translatable(CLEAR_ALL_KEY), this::handleClearAll)
                    .pos(TOTAL_BORDER, TOTAL_BORDER + 40)
                    .size(40, 20)
                    .tooltip(Tooltip.create(Component.translatable(CLEAR_ALL_TOOLTIP)))
                    .build());
            addRenderableWidget(new SmallCheckbox(
                    TOTAL_BORDER, TOTAL_BORDER + 60, 20, 20, Component.literal("DRC"), errorsShown,
                    newState -> {
                        errorsShown = newState;
                        updateErrors();
                    },
                    Tooltip.create(Component.translatable(DRC_INFO_KEY))
            ));
        }
        visibleArea.onSizeChanged(width, height);
    }

    private void handleSetName(Button $) {
        Minecraft.getInstance().setScreen(DataProviderScreen.makeFor(
                Component.translatable(SET_NAME_MESSAGE),
                schematic.getName(),
                MyCodecs.STRING,
                s -> runAndSendToServer(new SetName(s))
        ));
    }

    private void handleClearAll(Button $) {
        Minecraft.getInstance().setScreen(new ConfirmScreen(
                Component.translatable(CLEAR_ALL_MESSAGE), () -> runAndSendToServer(new ClearAll())
        ));
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        drawCenteredString(matrixStack, minecraft.font, schematic.getName(), width / 2, TOTAL_BORDER - 11, -1);

        visibleArea.setUpForDrawing(matrixStack);
        drawErrors(matrixStack);
        drawBoundary(matrixStack);
        Vec2d mousePos = visibleArea.getMousePositionInSchematic(mouseX, mouseY);
        ClientSymbols.render(schematic, matrixStack, mousePos);

        Optional<Component> currentError = placementHandler.renderFloatingAndGetError(
                matrixStack, mousePos, schematic
        );
        if (placementHandler.isDragSelecting(GLFW_MOUSE_BUTTON_LAST + 1)) {
            renderSelectedArea(matrixStack, mousePos);
        }
        RenderSystem.disableScissor();

        matrixStack.popPose();
        renderIngredients(matrixStack);
        renderTooltip(matrixStack, mouseX, mouseY, mousePos, currentError.orElse(null));
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
                            Component.translatable(key, pin.pinName())
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
            info = Component.literal(Math.min(available.count(), required) + " / " + required);
            if (available.count() < required) {
                info.withStyle(ChatFormatting.RED);
            }
        } else {
            info = Component.literal(Integer.toString(required));
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

    private void renderSelectedArea(PoseStack transform, Vec2d mousePos) {
        final var mouseDownPos = visibleArea.getMousePositionInSchematic((int) mouseXDown, (int) mouseYDown).floor();
        final var roundedMousePos = mousePos.floor();
        if (mouseDownPos.equals(roundedMousePos)) { return; }
        final var selectedRect = new RectangleI(mouseDownPos, roundedMousePos);
        ScreenUtils.drawBordersOutside(
                transform, selectedRect.minX(), selectedRect.minY(), selectedRect.maxX(), selectedRect.maxY(),
                1 / visibleArea.getCurrentScale(), -1
        );
        ScreenUtils.fill(
                transform,
                selectedRect.minX(), selectedRect.minY(), selectedRect.maxX(), selectedRect.maxY(),
                0x80ffffff
        );
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
        final float offset = 2 / visibleArea.getCurrentScale();
        ScreenUtils.drawBordersOutside(
                transform,
                Schematic.GLOBAL_MIN, Schematic.GLOBAL_MIN, Schematic.GLOBAL_MAX, Schematic.GLOBAL_MAX,
                offset, color
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            clickState = ClickState.CLICKED_CONSUMED;
            return true;
        }
        mouseXDown = mouseX;
        mouseYDown = mouseY;
        clickState = ClickState.CLICKED_UNDECIDED;
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (super.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        if (clickState == ClickState.NOT_CLICKED || clickState == ClickState.CLICKED_CONSUMED) { return false; }
        final Vec2d mousePos = visibleArea.getMousePositionInSchematic((int) mouseX, (int) mouseY);
        final var oldClickState = clickState;
        clickState = ClickState.NOT_CLICKED;
        if (oldClickState == ClickState.CLICKED_DRAGGING) {
            if (!placementHandler.isDragSelecting(button)) { return false; }
            final Vec2d oldMousePos = visibleArea.getMousePositionInSchematic((int) mouseXDown, (int) mouseYDown);
            final var selectedArea = new RectangleI(mousePos.floor(), oldMousePos.floor());
            if (!placementHandler.takePlacingFromArea(selectedArea, mousePos, getSchematic())) { return false; }
            runAndSendToServer(new DeleteArea(selectedArea));
            return true;
        }
        if (!placementHandler.placeCurrentlyHeld(mousePos, schematic, this::runAndSendToServer)) {
            var clicked = schematic.getSymbolAt(mousePos, minecraft.level);
            if (clicked != null) {
                handleSymbolClick(clicked, clicked.symbol(), mousePos, button);
            } else if (!container.readOnly) {
                placementHandler.startWire(mousePos);
            }
        }
        return true;
    }

    private <State>
    void handleSymbolClick(PlacedSymbol clicked, SymbolInstance<State> instance, Vec2d mousePos, int button) {
        if (!getHoveredPins(clicked, mousePos).isEmpty() && !container.readOnly) {
            placementHandler.startWire(mousePos);
            return;
        }
        if (button == GLFW_MOUSE_BUTTON_LEFT && !container.readOnly) {
            placementHandler.pickupComponent(clicked, mousePos, this::runAndSendToServer);
        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            if (!container.readOnly || instance.getType().canConfigureOnReadOnly()) {
                ClientSymbols.createInstanceWithUI(
                        instance.getType(),
                        newInst -> runAndSendToServer(new ModifySymbol(new PlacedSymbol(clicked.position(), newInst))),
                        instance.getCurrentState()
                );
            }
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        if (clickState != ClickState.CLICKED_DRAGGING) {
            if (!(Math.abs(mouseX - mouseXDown) > 1) && !(Math.abs(mouseY - mouseYDown) > 1)) {
                return false;
            }
            clickState = ClickState.CLICKED_DRAGGING;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            visibleArea.move(dragX, dragY);
            return true;
        }
        return placementHandler.isDragSelecting(button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!super.mouseScrolled(mouseX, mouseY, delta)) {
            visibleArea.onScroll(delta);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!container.readOnly) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (placementHandler.putBackOnEsc(getSchematic(), this::runAndSendToServer)) {
                    return true;
                }
            } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
                if (placementHandler.clearPlacingSymbol()) {
                    return true;
                }
                final Vec2d mousePos = visibleArea.getMousePositionInSchematic(ScreenUtils.getMousePosition());
                if (schematic.removeOneContaining(mousePos, minecraft.level)) {
                    sendToServer(new Delete(mousePos));
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderCustomBackground(
            @Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks
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

    public void setSchematic(Schematic schematic) {
        this.schematic = schematic;
        visibleArea.autoRange(schematic);
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

    private enum ClickState {
        NOT_CLICKED, CLICKED_UNDECIDED, CLICKED_CONSUMED, CLICKED_DRAGGING
    }
}
