package malte0811.controlengineering.tiles.tape;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.ItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;

import java.util.Arrays;
import java.util.List;

public class TeletypeState {
    public static final Codec<TeletypeState> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.BYTE.listOf().fieldOf("data").forGetter(TeletypeState::getData),
                    Codec.INT.fieldOf("available").forGetter(TeletypeState::getAvailable),
                    Codec.INT.fieldOf("numErased").forGetter(TeletypeState::getErased)
            ).apply(inst, TeletypeState::new)
    );

    private final ByteList data;
    private int available;
    private int numErased;

    public TeletypeState() {
        this(ImmutableList.of(), 0, 0);
    }

    public TeletypeState(List<Byte> data, int available, int numErased) {
        this(new ByteArrayList(data), available, numErased);
    }

    public TeletypeState(ByteList data, int available, int numErased) {
        this.data = data;
        this.available = available;
        this.numErased = numErased;
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

    public int getErased() {
        return numErased;
    }

    public void setErased(int numErased) {
        this.numErased = numErased;
    }

    public void addAvailable(int length) {
        setAvailable(getAvailable() + length);
    }

    public ActionResultType removeWrittenTape(PlayerEntity player) {
        ByteList written = getData();
        if (!written.isEmpty() && player != null) {
            ItemUtil.giveOrDrop(player, PunchedTapeItem.withBytes(written.toByteArray()));
            written.clear();
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    public ActionResultType removeOrAddClearTape(PlayerEntity player, ItemStack item) {
        final int length = EmptyTapeItem.getLength(item);
        if (length > 0) {
            //TODO limit?
            addAvailable(length);
            item.shrink(1);
        } else if (getAvailable() > 0 && player != null) {
            ItemUtil.giveOrDrop(player, EmptyTapeItem.withLength(getAvailable()));
            setAvailable(0);
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * @return Number of bytes typed
     */
    public int tryTypeAll(ByteList bytes) {
        final int numLost = Math.min(bytes.size(), getErased());
        setErased(getErased() - numLost);
        byte[] erasedData = new byte[numLost];
        Arrays.fill(erasedData, BitUtils.fixParity((byte) 0xff));
        getData().addElements(getData().size(), erasedData);
        final int numPrinted = Math.min(bytes.size() - numLost, getAvailable());
        setAvailable(getAvailable() - numPrinted);
        getData().addAll(bytes.subList(numLost, numLost + numPrinted));
        return numLost + numPrinted;
    }

    public boolean tryTypeChar(byte typed) {
        return tryTypeAll(ByteLists.singleton(typed)) >= 1;
    }
}
