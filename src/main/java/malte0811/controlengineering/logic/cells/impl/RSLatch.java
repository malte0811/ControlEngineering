package malte0811.controlengineering.logic.cells.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.logic.cells.Pin;
import malte0811.controlengineering.logic.cells.SignalType;
import net.minecraft.util.ResourceLocation;

import java.util.Random;

public class RSLatch extends LeafcellType<Boolean> {
    private static final Random RAND = new Random();

    public RSLatch() {
        super(
                new ResourceLocation(ControlEngineering.MODID, "rs_latch"),
                ImmutableList.of(
                        new Pin("reset", SignalType.DIGITAL), new Pin("set", SignalType.DIGITAL)
                ),
                ImmutableList.of(
                        new Pin("q", SignalType.DIGITAL), new Pin("not_q", SignalType.DIGITAL)
                ),
                false,
                Codec.BOOL
        );
    }

    @Override
    public Boolean nextState(DoubleList inputSignals, Boolean currentState) {
        boolean r = r(inputSignals);
        boolean s = s(inputSignals);
        if (r && s) {
            return RAND.nextBoolean();
        } else if (r) {
            return false;
        } else if (s) {
            return true;
        } else {
            return currentState;
        }
    }

    @Override
    public DoubleList getOutputSignals(DoubleList inputSignals, Boolean currentState) {
        final double q = currentState ? 1 : 0;
        return new DoubleArrayList(new double[]{q, 1 - q});
    }

    private boolean r(DoubleList input) {
        return bool(input.getDouble(0));
    }

    private boolean s(DoubleList input) {
        return bool(input.getDouble(1));
    }
}
