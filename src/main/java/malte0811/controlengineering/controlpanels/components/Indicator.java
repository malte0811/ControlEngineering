package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.util.math.Vec2d;
import net.minecraft.world.InteractionResult;

public class Indicator extends PanelComponentType<ColorAndSignal, Integer> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.indicator";

    public Indicator() {
        super(
                ColorAndSignal.DEFAULT, 0,
                ColorAndSignal.CODEC, Codec.INT,
                new Vec2d(1, 1), -1,
                TRANSLATION_KEY
        );
    }

    @Override
    public Integer updateTotalState(ColorAndSignal colorAndSignal, Integer oldState, BusState busState) {
        return busState.getSignal(colorAndSignal.signal());
    }

    @Override
    public Pair<InteractionResult, Integer> click(ColorAndSignal colorAndSignal, Integer oldState, boolean sneaking) {
        return Pair.of(InteractionResult.PASS, oldState);
    }
}
