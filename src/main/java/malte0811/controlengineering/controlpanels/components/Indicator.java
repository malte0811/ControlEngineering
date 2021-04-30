package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class Indicator extends PanelComponentType<ColorAndSignal, Integer> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.indicator";

    public Indicator() {
        super(
                ColorAndSignal.DEFAULT, 0,
                ColorAndSignal.CODEC, Codec.INT,
                new Vec2i(1, 1),
                TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(ColorAndSignal colorAndSignal, Integer integer) {
        return BusState.EMPTY;
    }

    @Override
    public Integer updateTotalState(ColorAndSignal colorAndSignal, Integer oldState, BusState busState) {
        return busState.getSignal(colorAndSignal.getSignal());
    }

    @Override
    public Integer tick(ColorAndSignal colorAndSignal, Integer oldState) {
        return oldState;
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return null;
    }

    @Override
    public Pair<ActionResultType, Integer> click(ColorAndSignal colorAndSignal, Integer oldState) {
        return Pair.of(ActionResultType.PASS, oldState);
    }
}
