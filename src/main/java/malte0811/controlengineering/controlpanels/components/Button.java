package malte0811.controlengineering.controlpanels.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.controlpanels.PanelComponent;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;

public class Button extends PanelComponent<Button> {
    public int color;

    public Button(int color) {
        this.color = color;
    }

    public Button() {
        this(-1);
    }

    @Override
    public BusState getEmittedState() {
        return new BusState(1);
    }

    @Override
    public void updateTotalState(BusState state) {}

    public static Codec<Button> createCodec() {
        return RecordCodecBuilder.create(
                inst -> inst.group(
                        Codec.INT.fieldOf("color").forGetter(b -> b.color)
                ).apply(inst, Button::new)
        );
    }

    @Nullable
    @Override
    protected AxisAlignedBB createSelectionShape() {
        return new AxisAlignedBB(0, 0, 0, 1, 0.5, 1);
    }

    public void setColor(int color) {
        this.color = color;
    }
}
