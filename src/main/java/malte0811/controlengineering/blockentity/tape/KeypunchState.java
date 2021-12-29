package malte0811.controlengineering.blockentity.tape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.serialization.Codecs;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class KeypunchState {
    private final Runnable markDirty;
    private final Data data;

    public KeypunchState(Runnable markDirty) {
        this(markDirty, new Data());
    }

    public KeypunchState(Runnable markDirty, Tag nbt) {
        this(markDirty, Codecs.readOptional(Data.CODEC, nbt).orElseGet(Data::new));
    }

    private KeypunchState(Runnable markDirty, Data data) {
        this.markDirty = markDirty;
        this.data = data;
    }

    public ByteList getData() {
        return data.data;
    }

    public int getAvailable() {
        return data.available;
    }

    public void setAvailable(int available) {
        data.available = available;
        this.markDirty.run();
    }

    public int getErased() {
        return data.numErased;
    }

    public void setErased(int numErased) {
        data.numErased = numErased;
        this.markDirty.run();
    }

    public void addAvailable(int length) {
        setAvailable(getAvailable() + length);
    }

    public InteractionResult removeWrittenTape(Player player) {
        ByteList written = getData();
        if (!written.isEmpty() && player != null) {
            ItemUtil.giveOrDrop(player, PunchedTapeItem.withBytes(written.toByteArray()));
            written.clear();
            this.markDirty.run();
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
        this.markDirty.run();
        return numLost + numPrinted;
    }

    public boolean tryTypeChar(byte typed, boolean fixParity) {
        return tryTypeAll(ByteLists.singleton(fixParity ? BitUtils.fixParity(typed) : typed)) >= 1;
    }

    public Tag toNBT() {
        return Codecs.encode(Data.CODEC, data);
    }

    private static class Data {
        private static final Codec<Data> CODEC = RecordCodecBuilder.create(
                inst -> inst.group(
                        Codecs.BYTE_LIST_CODEC.fieldOf("data").forGetter(d -> d.data),
                        Codec.INT.fieldOf("available").forGetter(d -> d.available),
                        Codec.INT.fieldOf("numErased").forGetter(d -> d.numErased)
                ).apply(inst, Data::new)
        );
        private final ByteList data;
        private int available;
        private int numErased;

        private Data(ByteList data, int available, int numErased) {
            this.data = data;
            this.available = available;
            this.numErased = numErased;
        }

        private Data() {
            this(new ByteArrayList(), 0, 0);
        }
    }
}
