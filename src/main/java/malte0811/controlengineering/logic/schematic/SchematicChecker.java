package malte0811.controlengineering.logic.schematic;

import it.unimi.dsi.fastutil.ints.IntSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static malte0811.controlengineering.logic.schematic.Schematic.BOUNDARY;

public class SchematicChecker {
    public static final String WIRE_OUTSIDE_BOUNDARY = ControlEngineering.MODID + ".gui.wireOutsideBoundary";
    public static final String SYMBOL_OUTSIDE_BOUNDARY = ControlEngineering.MODID + ".gui.symbolOutsideBoundary";
    public static final String MULTIPLE_SOURCES = ControlEngineering.MODID + ".gui.multipleSources";
    public static final String CYCLE = ControlEngineering.MODID + ".gui.cycle";
    public static final String ANALOG_DIGITAL_MIX = ControlEngineering.MODID + ".gui.analogVsDigital";
    public static final String SYMBOL_INTERSECTION = ControlEngineering.MODID + ".gui.symbolIntersection";
    private final Schematic schematic;

    public SchematicChecker(Schematic schematic) {
        this.schematic = schematic;
    }

    public Optional<ITextComponent> getErrorForAdding(WireSegment segment) {
        if (!BOUNDARY.containsClosed(segment.getStart()) || !BOUNDARY.containsClosed(segment.getEnd())) {
            return error(WIRE_OUTSIDE_BOUNDARY);
        }
        IntSet netsToCheck = schematic.getConnectedNetIndices(segment);
        Set<ConnectedPin> wirePins = new SchematicNet(segment).computeConnectedPins(schematic.getSymbols());
        Set<ConnectedPin> allPins = new HashSet<>(wirePins);
        for (int netId : netsToCheck) {
            SchematicNet net = schematic.getNets().get(netId);
            allPins.addAll(net.getOrComputePins(schematic.getSymbols()));
        }
        return getConsistencyError(allPins);
    }

    public boolean canAdd(WireSegment segment) {
        return !getErrorForAdding(segment).isPresent();
    }

    public static Optional<ITextComponent> getConsistencyError(Set<ConnectedPin> netPins) {
        ConnectedPin sourcePin = null;
        boolean hasAnalogSource = false;
        boolean hasDigitalSink = false;
        int leftmostX = Integer.MAX_VALUE;
        for (ConnectedPin pin : netPins) {
            if (pin.getPin().isOutput()) {
                if (sourcePin != null) {
                    // Only allow one signal source
                    return error(MULTIPLE_SOURCES);
                }
                sourcePin = pin;
                if (pin.isAnalog()) {
                    hasAnalogSource = true;
                }
            } else if (!pin.isAnalog()) {
                hasDigitalSink = true;
            }
            if (leftmostX > pin.getPosition().x) {
                leftmostX = pin.getPosition().x;
            }
        }
        if (sourcePin != null && sourcePin.getPin().isCombinatorialOutput() && sourcePin.getPosition().x > leftmostX) {
            // there are pins left of the source pin
            return error(CYCLE);
        }
        // Do not allow analog source with digital sink
        if (hasAnalogSource && hasDigitalSink) {
            return error(ANALOG_DIGITAL_MIX);
        } else {
            return Optional.empty();
        }
    }

    public static boolean isConsistent(Set<ConnectedPin> netPins) {
        return !getConsistencyError(netPins).isPresent();
    }

    public Optional<ITextComponent> getErrorForAdding(PlacedSymbol candidate) {
        if (!BOUNDARY.contains(candidate.getShape())) {
            return error(SYMBOL_OUTSIDE_BOUNDARY);
        }
        if (!schematic.getSymbols().stream().allMatch(candidate::canCoexist)) {
            return error(SYMBOL_INTERSECTION);
        }
        for (SchematicNet net : schematic.getNets()) {
            Set<ConnectedPin> pinsInNet = new HashSet<>(net.getOrComputePins(schematic.getSymbols()));
            pinsInNet.addAll(net.computeConnectedPins(Collections.singletonList(candidate)));
            Optional<ITextComponent> netConsistency = getConsistencyError(pinsInNet);
            if (netConsistency.isPresent()) {
                return netConsistency;
            }
        }
        return Optional.empty();
    }

    public boolean canAdd(PlacedSymbol candidate) {
        return !getErrorForAdding(candidate).isPresent();
    }

    private static Optional<ITextComponent> error(String translationKey) {
        return Optional.of(new TranslationTextComponent(translationKey));
    }
}
