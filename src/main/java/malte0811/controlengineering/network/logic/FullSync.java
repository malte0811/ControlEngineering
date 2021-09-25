package malte0811.controlengineering.network.logic;

import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class FullSync extends LogicSubPacket {
    private final Schematic schematic;

    public FullSync(Schematic schematic) {
        this.schematic = schematic;
    }

    public FullSync(FriendlyByteBuf in) {
        schematic = in.readWithCodec(Schematic.CODEC);
    }

    @Override
    public void write(FriendlyByteBuf out) {
        out.writeWithCodec(Schematic.CODEC, schematic);
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
