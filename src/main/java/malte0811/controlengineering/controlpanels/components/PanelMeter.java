package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2d;

public class PanelMeter extends PanelComponentType<BusSignalRef, Integer> {
    public static final Vec2d SIZE = new Vec2d(6, 4);

    public PanelMeter() {
        super(BusSignalRef.DEFAULT, 0, BusSignalRef.CODEC, Codec.INT, SIZE, -1);
    }

    @Override
    public Integer updateTotalState(BusSignalRef watchedSignal, Integer oldState, BusState busState) {
        return busState.getSignal(watchedSignal);
    }
}
