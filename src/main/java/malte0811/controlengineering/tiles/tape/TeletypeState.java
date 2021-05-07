package malte0811.controlengineering.tiles.tape;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

import java.util.List;

public class TeletypeState {
    public static final Codec<TeletypeState> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.BYTE.listOf().fieldOf("data").forGetter(TeletypeState::getData),
                    Codec.INT.fieldOf("available").forGetter(TeletypeState::getAvailable)
            ).apply(inst, TeletypeState::new)
    );

    private final ByteList data;
    private int available;

    public TeletypeState() {
        this(ImmutableList.of(), 0);
    }

    public TeletypeState(List<Byte> data, int available) {
        this(new ByteArrayList(data), available);
    }

    public TeletypeState(ByteList data, int available) {
        this.data = data;
        this.available = available;
    }

    public ByteList getData() {
        return data;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public void addAvailable(int length) {
        setAvailable(getAvailable() + length);
    }
}
