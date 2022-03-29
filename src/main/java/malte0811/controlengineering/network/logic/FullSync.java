package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.mycodec.serial.PacketBufferStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class FullSync extends LogicSubPacket {
    private final Schematic schematic;

    public FullSync(Schematic schematic) {
        this.schematic = schematic;
    }

    public FullSync(FriendlyByteBuf in) {
        schematic = Schematic.CODEC.fromSerial(new PacketBufferStorage(in)).get();
    }

    @Override
    public void write(FriendlyByteBuf out) {
        Schematic.CODEC.toSerial(new PacketBufferStorage(out), schematic);
    }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        replace.accept(schematic);
        return true;
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
