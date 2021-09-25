package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.FriendlyByteBuf;
import java.io.IOException;
import java.util.function.Consumer;

public class FullSync extends LogicSubPacket {
    private final Schematic schematic;

    public FullSync(Schematic schematic) {
        this.schematic = schematic;
    }

    public FullSync(FriendlyByteBuf in) {
        try {
            schematic = in.readWithCodec(Schematic.CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(FriendlyByteBuf out) {
        try {
            out.writeWithCodec(Schematic.CODEC, schematic);
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
