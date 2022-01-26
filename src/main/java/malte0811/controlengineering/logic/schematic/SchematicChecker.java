package malte0811.controlengineering.logic.schematic;

import it.unimi.dsi.fastutil.ints.IntSet;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.schematic.symbol.PlacedSymbol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

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

    public Optional<Component> getErrorForAdding(WireSegment segment) {
        if (!BOUNDARY.containsClosed(segment.start()) || !BOUNDARY.containsClosed(segment.end())) {
            return error(WIRE_OUTSIDE_BOUNDARY);
        }
        IntSet netsToCheck = schematic.getConnectedNetIndices(segment);
        Set<ConnectedPin> wirePins = new SchematicNet(segment).computeConnectedPins(schematic.getSymbols());
        Set<ConnectedPin> allPins = new HashSet<>(wirePins);
        for (int netId : netsToCheck) {
            SchematicNet net = schematic.getNets().get(netId);
            allPins.addAll(net.getOrComputePins(schematic.getSymbols()));
        }
        Optional<Component> consistency = getConsistencyError(allPins);
        if (consistency.isPresent()) {
            return consistency;
        }
        if (netsToCheck.size() + wirePins.size() > 1) {
            List<Collection<ConnectedPin>> nets = new ArrayList<>();
            nets.add(new ArrayList<>(allPins));
            for (int i = 0; i < schematic.getNets().size(); ++i) {
                if (!netsToCheck.contains(i)) {
                    nets.add(schematic.getNets().get(i).getOrComputePins(schematic.getSymbols()));
                }
            }
            if (!SchematicCircuitConverter.getCellOrder(
                    schematic.getSymbols(), SchematicCircuitConverter.getNetsBySource(nets)
            ).isPresent()) {
                return error(CYCLE);
            }
        }
        return Optional.empty();
    }

    public boolean canAdd(WireSegment segment) {
        return !getErrorForAdding(segment).isPresent();
    }

    public static Optional<Component> getConsistencyError(Set<ConnectedPin> netPins) {
        ConnectedPin sourcePin = null;
        boolean hasAnalogSource = false;
        boolean hasDigitalSink = false;
        for (ConnectedPin pin : netPins) {
            if (pin.pin().isOutput()) {
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
        }
        // Do not allow analog source with digital sink
        if (hasAnalogSource && hasDigitalSink) {
            return error(ANALOG_DIGITAL_MIX);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Component> getErrorForAdding(PlacedSymbol candidate) {
        if (!BOUNDARY.contains(candidate.getShape())) {
            return error(SYMBOL_OUTSIDE_BOUNDARY);
        }
        if (!schematic.getSymbols().stream().allMatch(candidate::canCoexist)) {
            return error(SYMBOL_INTERSECTION);
        }
        List<Collection<ConnectedPin>> nets = new ArrayList<>();
        for (SchematicNet net : schematic.getNets()) {
            Set<ConnectedPin> pinsInNet = new HashSet<>(net.getOrComputePins(schematic.getSymbols()));
            pinsInNet.addAll(net.computeConnectedPins(Collections.singletonList(candidate)));
            Optional<Component> netConsistency = getConsistencyError(pinsInNet);
            if (netConsistency.isPresent()) {
                return netConsistency;
            }
            nets.add(pinsInNet);
        }
        List<PlacedSymbol> allSymbols = new ArrayList<>(schematic.getSymbols());
        allSymbols.add(candidate);
        if (!SchematicCircuitConverter.getCellOrder(
                allSymbols, SchematicCircuitConverter.getNetsBySource(nets)
        ).isPresent()) {
            return error(CYCLE);
        }
        return Optional.empty();
    }

    public boolean canAdd(PlacedSymbol candidate) {
        return !getErrorForAdding(candidate).isPresent();
    }

    private static Optional<Component> error(String translationKey) {
        return Optional.of(new TranslatableComponent(translationKey));
    }
}
