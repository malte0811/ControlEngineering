package malte0811.controlengineering.controlpanels.components;

import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import malte0811.controlengineering.util.mycodec.MyCodecs;

public class Indicator extends PanelComponentType<ColorAndSignal, Integer> {
    public Indicator() {
        super(
                ColorAndSignal.DEFAULT, 0,
                ColorAndSignal.CODEC, MyCodecs.INTEGER,
                new Vec2d(1, 1), -1
        );
    }

    @Override
    public Integer updateTotalState(ColorAndSignal colorAndSignal, Integer oldState, BusState busState) {
        return busState.getSignal(colorAndSignal.signal());
    }
}
