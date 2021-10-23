package malte0811.controlengineering.logic.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public record PinReference(int cell, boolean isOutput, String pinName) {
    public static final Codec<PinReference> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("cell").forGetter(PinReference::cell),
                    Codec.BOOL.fieldOf("isOut").forGetter(PinReference::isOutput),
                    Codec.STRING.fieldOf("pin").forGetter(PinReference::pinName)
            ).apply(inst, PinReference::new)
    );
}
