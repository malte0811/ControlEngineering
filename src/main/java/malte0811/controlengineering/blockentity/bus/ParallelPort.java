package malte0811.controlengineering.blockentity.bus;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

//TODO extra clock line/ClockInstance? Would allow sending zero-bytes
public class ParallelPort {
    private final ByteList remotePrintQueue;
    private byte lastBusState;
    private boolean lastTickHigh;

    public ParallelPort() {
        remotePrintQueue = new ByteArrayList();
        lastTickHigh = false;
        lastBusState = 0;
    }

    public ParallelPort(CompoundTag nbt) {
        remotePrintQueue = new ByteArrayList(nbt.getByteArray("remotePrintQueue"));
        lastBusState = nbt.getByte("lastBusState");
        lastTickHigh = nbt.getBoolean("lastTickHigh");
    }

    public boolean tick() {
        //TODO receiving???

        if (lastTickHigh) {
            remotePrintQueue.removeByte(0);
            lastTickHigh = false;
            return true;
        }
        if (!remotePrintQueue.isEmpty()) {
            lastTickHigh = true;
            return true;
        }
        return false;
    }

    public BusState getOutputState() {
        if (!lastTickHigh) {
            return BusState.EMPTY;
        }
        byte toSend = remotePrintQueue.getByte(0);
        int[] line = new int[BusLine.LINE_SIZE];
        for (int i = 0; i < Byte.SIZE; ++i) {
            line[i] = BitUtils.getBit(toSend, i) ? BusLine.MAX_VALID_VALUE : BusLine.MIN_VALID_VALUE;
        }
        return BusState.EMPTY.withLine(0, new BusLine(line));
    }

    public Optional<Byte> onBusStateChange(BusState inputState) {
        byte inputByte = 0;
        for (int i = 0; i < Byte.SIZE; ++i) {
            if (inputState.getLine(0).getValue(i) != 0) {
                inputByte |= 1 << i;
            }
        }
        Optional<Byte> result = Optional.empty();
        if (lastBusState == 0 && inputByte != 0) {
            result = Optional.of(inputByte);
        }
        lastBusState = inputByte;
        return result;
    }

    public CompoundTag toNBT() {
        CompoundTag result = new CompoundTag();
        result.putByteArray("remotePrintQueue", remotePrintQueue.toByteArray());
        result.putByte("lastBusState", lastBusState);
        result.putBoolean("lastTickHigh", lastTickHigh);
        return result;
    }

    public void queueChar(byte out) {
        remotePrintQueue.add(out);
    }
}
