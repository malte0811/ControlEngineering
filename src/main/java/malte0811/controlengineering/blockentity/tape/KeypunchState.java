package malte0811.controlengineering.blockentity.tape;

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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class KeypunchState {
    public static final Codec<KeypunchState> CODEC = RecordCodecBuilder.create(
            inst -> inst.group(
                    Codec.BYTE.listOf().fieldOf("data").forGetter(KeypunchState::getData),
                    Codec.INT.fieldOf("available").forGetter(KeypunchState::getAvailable),
                    Codec.INT.fieldOf("numErased").forGetter(KeypunchState::getErased)
            ).apply(inst, KeypunchState::new)
    );

    private final ByteList data;
    private int available;
    private int numErased;

    public KeypunchState() {
        this(ImmutableList.of(), 0, 0);
    }

    public KeypunchState(List<Byte> data, int available, int numErased) {
        this(new ByteArrayList(data), available, numErased);
    }

    public KeypunchState(ByteList data, int available, int numErased) {
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

    public InteractionResult removeWrittenTape(Player player) {
        ByteList written = getData();
        if (!written.isEmpty() && player != null) {
            ItemUtil.giveOrDrop(player, PunchedTapeItem.withBytes(written.toByteArray()));
            written.clear();
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public InteractionResult removeOrAddClearTape(Player player, ItemStack item) {
        final int length = EmptyTapeItem.getLength(item);
        if (length > 0) {
            //TODO limit?
            addAvailable(length);
            item.shrink(1);
        } else if (getAvailable() > 0 && player != null) {
            ItemUtil.giveOrDrop(player, EmptyTapeItem.withLength(getAvailable()));
            setAvailable(0);
        }
        return InteractionResult.SUCCESS;
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

    public boolean tryTypeChar(byte typed, boolean fixParity) {
        return tryTypeAll(ByteLists.singleton(fixParity ? BitUtils.fixParity(typed) : typed)) >= 1;
    }
}
