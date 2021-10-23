package malte0811.controlengineering.tiles.panels;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionParser;

public record CNCJob(
        ImmutableList<PlacedComponent> components,
        IntList tickPlacingComponent,
        IntList tapeProgressAfterComponent,
        int totalTicks
) {
    public static CNCJob createFor(CNCInstructionParser.ParserResult parserData) {
        final int timePerComponent = 60;
        IntList tickEnds = new IntArrayList(parserData.getComponents().size());
        for (int i = 0; i < parserData.getComponentEnds().size(); ++i) {
            tickEnds.add(timePerComponent * (i + 1));
        }
        return new CNCJob(
                parserData.getComponents(),
                tickEnds,
                parserData.getComponentEnds(),
                timePerComponent * parserData.getComponentEnds().size() + timePerComponent / 2
        );
    }

    public CNCJob {
        Preconditions.checkArgument(components.size() == tickPlacingComponent.size());
        Preconditions.checkArgument(components.size() == tapeProgressAfterComponent.size());
    }

    public double getTapeProgressAtTime(double tick) {
        return interpolate(tick, tapeProgressAfterComponent::getInt);
    }

    public int getTotalComponents() {
        return components.size();
    }

    private double interpolate(double tick, Int2IntFunction getValue) {
        if (components.isEmpty()) {
            return 0;
        }
        final int nextToPlace = getNumComponentsAt(tick);
        if (nextToPlace >= components.size()) {
            return getValue.applyAsInt(getTotalComponents() - 1);
        }
        final int nextValue = getValue.applyAsInt(nextToPlace);
        final int nextTick = tickPlacingComponent.getInt(nextToPlace);
        final int lastValue;
        final int lastTick;
        if (nextToPlace > 0) {
            lastValue = getValue.applyAsInt(nextToPlace - 1);
            lastTick = tickPlacingComponent.getInt(nextToPlace - 1);
        } else {
            lastValue = 0;
            lastTick = 0;
        }
        final double deltaT = nextTick - lastTick;
        final double deltaValue = nextValue - lastValue;
        return lastValue + (tick - lastTick) / deltaT * deltaValue;
    }

    private int getNumComponentsAt(double tick) {
        for (int end = 0; end < getTotalComponents(); ++end) {
            if (tickPlacingComponent.getInt(end) > tick) {
                return end;
            }
        }
        return getTotalComponents();
    }
}
