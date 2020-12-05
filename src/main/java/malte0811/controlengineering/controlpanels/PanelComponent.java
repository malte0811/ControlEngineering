package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.Vec2d;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

public abstract class PanelComponent<Self extends PanelComponent<Self>> {
    public static Codec<PanelComponent<?>> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    // TODO skip NBT intermediate?
                    CompoundNBT.CODEC.fieldOf("data")
                            .forGetter(PanelComponent::toNBT),
                    ResourceLocation.CODEC.fieldOf("type")
                            .forGetter(t -> t.getType().getName())
            ).apply(inst, (nbt, typeID) -> {
                PanelComponentType<?> type = PanelComponents.getType(typeID);
                return type.fromNBT(nbt);
            })
    );

    private PanelComponentType<Self> type;
    // Pixel-relative
    private final Vec2d size;

    protected PanelComponent(Vec2d size) {
        this.size = size;
    }

    void setType(PanelComponentType<Self> type) {
        this.type = type;
    }

    public PanelComponentType<Self> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    private Self asSelf() {
        return (Self) this;
    }

    private CompoundNBT toNBT() {
        return getType().toNBT(asSelf());
    }

    public abstract BusState getEmittedState();

    public abstract void updateTotalState(BusState state);

    @Nullable
    protected abstract AxisAlignedBB createSelectionShape();

    private final Lazy<AxisAlignedBB> shape = Lazy.of(this::createSelectionShape);

    @Nullable
    public final AxisAlignedBB getSelectionBox() {
        return shape.get();
    }

    public Vec2d getSize() {
        return size;
    }

    public abstract ActionResultType onClick();
}
