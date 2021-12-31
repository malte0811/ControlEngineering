package malte0811.controlengineering.blockentity.bus;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.bus.BusState;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class ParallelPort {
    private static final int BUS_LINE = 0;
    private static final int CLOCK_INDEX = Byte.SIZE;

    private final ByteList transmitQueue;
    private boolean sendingFirst;
    private byte currentInput;
    private boolean triggerHigh;

    public ParallelPort() {
        transmitQueue = new ByteArrayList();
    }

    public ParallelPort(CompoundTag nbt) {
        transmitQueue = new ByteArrayList(nbt.getByteArray("remotePrintQueue"));
        sendingFirst = nbt.getBoolean("sendingFirst");
        currentInput = nbt.getByte("currentInput");
        triggerHigh = nbt.getBoolean("triggerHigh");
    }

    public boolean tickTX() {
        boolean updateRS = false;
        if (sendingFirst && !transmitQueue.isEmpty()) {
            transmitQueue.removeByte(0);
            sendingFirst = false;
            updateRS = true;
        }
        if (!transmitQueue.isEmpty()) {
            sendingFirst = true;
            updateRS = true;
        }
        return updateRS;
    }

    public Optional<Byte> tickRX() {
        if (triggerHigh) {
            return Optional.of(currentInput);
        } else {
            return Optional.empty();
        }
    }

    public BusState getOutputState() {
        if (!sendingFirst || transmitQueue.isEmpty()) {
            return BusState.EMPTY;
        }
        byte toSend = transmitQueue.getByte(0);
        int[] line = new int[BusLine.LINE_SIZE];
        for (int i = 0; i < Byte.SIZE; ++i) {
            line[i] = BitUtils.getBit(toSend, i) ? BusLine.MAX_VALID_VALUE : BusLine.MIN_VALID_VALUE;
        }
        line[CLOCK_INDEX] = BusLine.MAX_VALID_VALUE;
        return BusState.EMPTY.withLine(BUS_LINE, new BusLine(line));
    }

    public void onBusStateChange(BusState inputState) {
        triggerHigh = inputState.getLine(BUS_LINE).getValue(CLOCK_INDEX) != 0;
        currentInput = 0;
        for (int i = 0; i < Byte.SIZE; ++i) {
            if (inputState.getLine(BUS_LINE).getValue(i) != 0) {
                currentInput |= 1 << i;
            }
        }
    }

    public CompoundTag toNBT() {
        CompoundTag result = new CompoundTag();
        result.putByteArray("remotePrintQueue", transmitQueue.toByteArray());
        result.putBoolean("sendingFirst", sendingFirst);
        result.putByte("currentInput", currentInput);
        result.putBoolean("triggerHigh", triggerHigh);
        return result;
    }

    public void queueChar(byte out) {
        transmitQueue.add(out);
    }
}
