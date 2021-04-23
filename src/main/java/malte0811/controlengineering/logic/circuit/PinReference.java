package malte0811.controlengineering.logic.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class PinReference {
    public static final Codec<PinReference> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("cell").forGetter(r -> r.cell),
                    Codec.BOOL.fieldOf("isOut").forGetter(r -> r.isOutput),
                    Codec.INT.fieldOf("pin").forGetter(r -> r.pin)
            ).apply(inst, PinReference::new)
    );

    private final int cell;
    private final boolean isOutput;
    private final int pin;

    public PinReference(int cell, boolean isOutput, int pin) {
        this.cell = cell;
        this.isOutput = isOutput;
        this.pin = pin;
    }

    public int getCell() {
        return cell;
    }

    public boolean isOutput() {
        return isOutput;
    }

    public int getPin() {
        return pin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinReference that = (PinReference) o;
        return cell == that.cell && isOutput == that.isOutput && pin == that.pin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cell, isOutput, pin);
    }
}
