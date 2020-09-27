package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponent;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class Button extends PanelComponent<Button> {
    public int color;
    public boolean active;
    private BusSignalRef outputSignal;

    public Button(int color, boolean active, BusSignalRef outputSignal) {
        this.color = color;
        this.active = active;
        this.outputSignal = outputSignal;
    }

    public Button() {
        this(-1, false, new BusSignalRef(0, 0));
    }

    @Override
    public BusState getEmittedState() {
        if (active) {
            return outputSignal.singleSignalState(15);
        } else {
            return new BusState(1);
        }
    }

    @Override
    public void updateTotalState(BusState state) {}

    public static Codec<Button> createCodec() {
        return RecordCodecBuilder.create(
                inst -> inst.group(
                        Codec.INT.fieldOf("color").forGetter(b -> b.color),
                        Codec.BOOL.fieldOf("active").forGetter(b -> b.active),
                        BusSignalRef.CODEC.fieldOf("output").forGetter(b -> b.outputSignal)
                ).apply(inst, Button::new)
        );
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    }

    @Override
    public ActionResultType onClick() {
        this.active = !this.active;
        return ActionResultType.SUCCESS;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
