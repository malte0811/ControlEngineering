package malte0811.controlengineering.controlpanels.components;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponent;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class Indicator extends PanelComponent<Indicator> {
    private int color;
    private BusSignalRef signal;
    private int rsValue;

    public Indicator() {
        this(-1, new BusSignalRef(0, 0), 0);
    }

    public Indicator(int color, BusSignalRef signal, int rsValue) {
        this.color = color;
        this.signal = signal;
        this.rsValue = rsValue;
    }

    @Override
    public BusState getEmittedState() {
        return new BusState();
    }

    @Override
    public void updateTotalState(BusState state) {
        rsValue = state.getSignal(signal);
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return null;
    }

    @Override
    public ActionResultType onClick() {
        return ActionResultType.PASS;
    }

    public static Codec<Indicator> createCodec() {
        return RecordCodecBuilder.create(
                inst -> inst.group(
                        Codec.INT.fieldOf("color").forGetter(i -> i.color),
                        BusSignalRef.CODEC.fieldOf("input").forGetter(i -> i.signal),
                        Codec.INT.fieldOf("value").forGetter(i -> i.rsValue)
                ).apply(inst, Indicator::new)
        );
    }

    public int getColor() {
        return color;
    }

    public int getRsValue() {
        return rsValue;
    }
}
