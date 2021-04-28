package malte0811.controlengineering.logic.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class PinReference {
    public static final Codec<PinReference> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.INT.fieldOf("cell").forGetter(PinReference::getCell),
                    Codec.BOOL.fieldOf("isOut").forGetter(PinReference::isOutput),
                    Codec.STRING.fieldOf("pin").forGetter(PinReference::getPinName)
            ).apply(inst, PinReference::new)
    );

    private final int cell;
    private final boolean isOutput;
    private final String pinName;

    public PinReference(int cell, boolean isOutput, String pinName) {
        this.cell = cell;
        this.isOutput = isOutput;
        this.pinName = pinName;
    }

    public int getCell() {
        return cell;
    }

    public boolean isOutput() {
        return isOutput;
    }

    public String getPinName() {
        return pinName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinReference that = (PinReference) o;
        return cell == that.cell && isOutput == that.isOutput && pinName.equals(that.pinName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cell, isOutput, pinName);
    }
}
