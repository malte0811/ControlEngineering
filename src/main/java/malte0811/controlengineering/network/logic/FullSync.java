package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.function.Consumer;

public class FullSync extends LogicSubPacket {
    private final Schematic schematic;

    public FullSync(Schematic schematic) {
        this.schematic = schematic;
    }

    public FullSync(PacketBuffer in) {
        try {
            schematic = in.func_240628_a_(Schematic.CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(PacketBuffer out) {
        try {
            out.func_240629_a_(Schematic.CODEC, schematic);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void process(
            Schematic applyTo, Consumer<Schematic> replace
    ) {
        replace.accept(schematic);
    }

    @Override
    public boolean allowSendingToServer() {
        return false;
    }
}
