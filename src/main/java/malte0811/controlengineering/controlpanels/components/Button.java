package malte0811.controlengineering.controlpanels.components;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponentType;
import malte0811.controlengineering.util.math.Vec2i;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class Button extends PanelComponentType<ColorAndSignal, Boolean> {
    public static final String TRANSLATION_KEY = ControlEngineering.MODID + ".component.button";

    public Button() {
        super(
                ColorAndSignal.DEFAULT, false,
                ColorAndSignal.CODEC, Codec.BOOL,
                new Vec2i(1, 1),
                TRANSLATION_KEY
        );
    }

    @Override
    public BusState getEmittedState(ColorAndSignal config, Boolean active) {
        if (active) {
            return config.getSignal().singleSignalState(BusLine.MAX_VALID_VALUE);
        } else {
            return BusState.EMPTY;
        }
    }

    @Override
    public Boolean updateTotalState(ColorAndSignal config, Boolean oldState, BusState busState) {
        return oldState;
    }

    @Override
    public Boolean tick(ColorAndSignal config, Boolean oldState) {
        return oldState;
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    }

    @Override
    public Pair<ActionResultType, Boolean> click(ColorAndSignal config, Boolean oldState) {
        return Pair.of(ActionResultType.SUCCESS, !oldState);
    }
}
