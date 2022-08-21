package malte0811.controlengineering.gui.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.logic.schematic.WireSegment;
import malte0811.controlengineering.logic.schematic.client.ClientSymbols;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import malte0811.controlengineering.network.logic.Add;
import malte0811.controlengineering.network.logic.Delete;
import malte0811.controlengineering.network.logic.LogicSubPacket;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.util.Mth.ceil;
import static net.minecraft.util.Mth.floor;
import static org.lwjgl.glfw.GLFW.*;

public class PlacementHandler {
    private final Minecraft minecraft;

    @Nullable
    private Vec2i currentWireStart = null;
    @Nullable
    private PlacingSymbols placingSymbol = null;

    public PlacementHandler(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void setPlacingSymbol(SymbolInstance<?> symbol) {
        placingSymbol = new PlacingSymbols(new PlacedSymbol(Vec2i.ZERO, symbol), Vec2d.ZERO, false);
    }

    public Optional<Component> renderFloatingAndGetError(PoseStack transform, Vec2d mousePos, Schematic schematic) {
        if (placingSymbol != null) {
            var absSymbols = placingSymbol.absoluteSymbols(mousePos);
            var absWires = placingSymbol.absoluteWires(mousePos);
            for (PlacedSymbol s : absSymbols) {
                ClientSymbols.render(s, transform, 0x80);
            }
            new SchematicNet(absWires).render(transform, SchematicNet.MOVING_WIRE_COLOR, absSymbols);
            return schematic.makeChecker(minecraft.level).getErrorForAddingAll(absSymbols, absWires);
        } else {
            WireSegment placedWire = getPlacingSegment(mousePos);
            if (placedWire == null) { return Optional.empty(); }
            final var currentError = schematic.makeChecker(minecraft.level).getErrorForAdding(placedWire);
            final int color = currentError.isPresent() ? 0xffff5515 : 0xff785515;
            placedWire.renderWithoutBlobs(transform, color);
            return currentError;
        }
    }

    public boolean isDragSelecting() {
        if (placingSymbol != null || currentWireStart != null) {
            return false;
        }
        return glfwGetMouseButton(minecraft.getWindow().getWindow(), GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
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

    public boolean takePlacingFromArea(RectangleI selectedArea, Vec2d mousePos, Schematic schematic) {
        List<WireSegment> movedSegments = new ArrayList<>();
        for (final var segmentIdx : schematic.getWiresWithin(selectedArea)) {
            movedSegments.addAll(schematic.getWireSegments(segmentIdx));
        }
        List<PlacedSymbol> movedSymbols = new ArrayList<>();
        for (final var symbolIdx : schematic.getSymbolIndicesWithin(selectedArea, minecraft.level)) {
            movedSymbols.add(schematic.getSymbols().get(symbolIdx));
        }
        if (!movedSegments.isEmpty() || !movedSymbols.isEmpty()) {
            this.placingSymbol = new PlacingSymbols(movedSymbols, movedSegments, mousePos, true);
            return true;
        } else {
            return false;
        }
    }

    public boolean placeCurrentlyHeld(Vec2d mousePos, Schematic schematic, Consumer<LogicSubPacket> execPacket) {
        return tryPlaceSymbol(mousePos, schematic, execPacket) || tryPlaceWire(mousePos, schematic, execPacket);
    }

    public void startWire(Vec2d mousePos) {
        currentWireStart = mousePos.floor();
    }

    private boolean tryPlaceSymbol(Vec2d mousePos, Schematic schematic, Consumer<LogicSubPacket> execPacket) {
        if (placingSymbol == null) { return false; }
        final var symbols = placingSymbol.absoluteSymbols(mousePos);
        final var wires = placingSymbol.absoluteWires(mousePos);
        if (schematic.makeChecker(minecraft.level).getErrorForAddingAll(symbols, wires).isEmpty()) {
            execPacket.accept(new Add(wires, symbols));
            if (placingSymbol.movingExisting) {
                this.placingSymbol = null;
            }
        }
        return true;
    }

    private boolean tryPlaceWire(Vec2d mousePos, Schematic schematic, Consumer<LogicSubPacket> execPacket) {
        WireSegment placedWire = getPlacingSegment(mousePos);
        if (placedWire == null) { return false; }
        if (schematic.makeChecker(minecraft.level).canAdd(placedWire)) {
            execPacket.accept(new Add(List.of(placedWire), List.of()));
            if (placedWire.end().equals(currentWireStart)) {
                currentWireStart = placedWire.start();
            } else {
                currentWireStart = placedWire.end();
            }
        }
        return true;
    }

    public void pickupComponent(PlacedSymbol clicked, Vec2d mousePos, Predicate<LogicSubPacket> execPacket) {
        if (execPacket.test(new Delete(mousePos))) {
            placingSymbol = new PlacingSymbols(clicked, mousePos, true);
        }
    }

    public boolean putBackOnEsc(Schematic schematic, Consumer<LogicSubPacket> execPacket) {
        if (currentWireStart == null && placingSymbol == null) { return false; }
        if (placingSymbol != null && placingSymbol.movingExisting) {
            tryPlaceSymbol(placingSymbol.originalMousePos(), schematic, execPacket);
        }
        currentWireStart = null;
        placingSymbol = null;
        return true;
    }

    private record PlacingSymbols(
            List<PlacedSymbol> originalSymbols,
            List<WireSegment> originalWires,
            Vec2d originalMousePos,
            boolean movingExisting
    ) {
        public PlacingSymbols(PlacedSymbol symbol, Vec2d originalMousePos, boolean movingExisting) {
            this(List.of(symbol), List.of(), originalMousePos, movingExisting);
        }

        public List<PlacedSymbol> absoluteSymbols(Vec2d mousePos) {
            var offset = getTotalOffset(mousePos);
            List<PlacedSymbol> absoluteSymbols = new ArrayList<>(originalSymbols.size());
            for (var relative : originalSymbols) {
                absoluteSymbols.add(new PlacedSymbol(
                        relative.position().add(offset), relative.symbol()
                ));
            }
            return absoluteSymbols;
        }

        public List<WireSegment> absoluteWires(Vec2d mousePos) {
            var offset = getTotalOffset(mousePos);
            List<WireSegment> absoluteWires = new ArrayList<>(originalWires.size());
            for (var relative : originalWires) {
                absoluteWires.add(new WireSegment(relative.start().add(offset), relative.length(), relative.axis()));
            }
            return absoluteWires;
        }

        private Vec2i getTotalOffset(Vec2d mousePos) {
            return mousePos.subtract(originalMousePos).round();
        }
    }
}
