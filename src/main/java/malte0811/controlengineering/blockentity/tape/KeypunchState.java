package malte0811.controlengineering.blockentity.tape;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteLists;
import malte0811.controlengineering.items.EmptyTapeItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.ItemUtil;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import malte0811.controlengineering.util.mycodec.record.CodecField;
import malte0811.controlengineering.util.mycodec.record.RecordCodec3;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class KeypunchState {
    public static final int MAX_TAPE_LENGTH = 10_000;

    private final Runnable markDirty;
    private final Data data;

    public KeypunchState(Runnable markDirty) {
        this(markDirty, new Data());
    }

    public KeypunchState(Runnable markDirty, Tag nbt) {
        this(markDirty, Data.CODEC.fromNBT(nbt, Data::new));
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

    private boolean addAvailable(int length) {
        var newAvailable = length + getAvailable();
        if (getErased() + newAvailable > MAX_TAPE_LENGTH) {
            return false;
        }
        setAvailable(getAvailable() + length);
        return true;
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
            if (addAvailable(length)) {
                item.shrink(1);
            }
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
        if (getData().size() > MAX_TAPE_LENGTH) {
            getData().removeElements(MAX_TAPE_LENGTH - 1, getData().size());
        }
        this.markDirty.run();
        return numLost + numPrinted;
    }

    public boolean tryTypeChar(byte typed, boolean fixParity) {
        return tryTypeAll(ByteLists.singleton(fixParity ? BitUtils.fixParity(typed) : typed)) >= 1;
    }

    public Tag toNBT() {
        return Data.CODEC.toNBT(data);
    }

    private static class Data {
        private static final MyCodec<Data> CODEC = new RecordCodec3<>(
                new CodecField<>("data", d -> d.data, MyCodecs.BYTE_LIST),
                new CodecField<>("available", d -> d.available, MyCodecs.INTEGER),
                new CodecField<>("numErased", d -> d.numErased, MyCodecs.INTEGER),
                Data::new
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
