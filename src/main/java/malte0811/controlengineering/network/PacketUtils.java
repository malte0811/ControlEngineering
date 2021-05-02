package malte0811.controlengineering.network;

import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketUtils {
    public static <T> List<T> readList(PacketBuffer buffer, Function<PacketBuffer, T> readElement) {
        int numElements = buffer.readVarInt();
        List<T> ret = new ArrayList<>(numElements);
        for (int i = 0; i < numElements; ++i) {
            ret.add(readElement.apply(buffer));
        }
        return ret;
    }

    public static <T> void writeList(PacketBuffer buffer, List<T> toWrite, BiConsumer<T, PacketBuffer> writeElement) {
        buffer.writeVarInt(toWrite.size());
        for (T element : toWrite) {
            writeElement.accept(element, buffer);
        }
    }
}
