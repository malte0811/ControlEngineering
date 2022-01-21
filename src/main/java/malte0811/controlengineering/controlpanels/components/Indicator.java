package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;

public class Indicator extends PanelComponentType<ColorAndSignal, Integer> {
    public Indicator() {
        super(
                ColorAndSignal.DEFAULT, 0,
                ColorAndSignal.CODEC, Codec.INT,
                new Vec2d(1, 1), -1
        );
    }

    @Override
    public Integer updateTotalState(ColorAndSignal colorAndSignal, Integer oldState, BusState busState) {
        return busState.getSignal(colorAndSignal.signal());
    }
}
