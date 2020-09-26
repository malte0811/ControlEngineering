package malte0811.controlengineering.controlpanels;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.Codecs;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;

public abstract class PanelComponent<Self extends PanelComponent<Self>> {
    public static Codec<PanelComponent<?>> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    // TODO skip NBT intermediate?
                    Codecs.INBT_CODEC.fieldOf("data")
                            .forGetter(PanelComponent::toNBT),
                    ResourceLocation.CODEC.fieldOf("type")
                            .forGetter(t -> t.getType().getName())
            ).apply(inst, (nbt, typeID) -> {
                PanelComponentType<?> type = PanelComponents.getType(typeID);
                return type.fromNBT(nbt);
            })
    );

    private PanelComponentType<Self> type;

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

    private INBT toNBT() {
        return getType().toNBT(asSelf());
    }

    public abstract BusState getEmittedState();

    public abstract void updateTotalState(BusState state);
}
