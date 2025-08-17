package malte0811.controlengineering.network.logic;

import com.google.common.collect.Lists;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.util.math.RectangleI;
import malte0811.controlengineering.util.mycodec.MyCodec;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class DeleteArea extends LogicSubPacket {
    public static final MyCodec<DeleteArea> CODEC = RectangleI.CODEC.xmap(DeleteArea::new, da -> da.area);

    private final RectangleI area;

    public DeleteArea(RectangleI area) { this.area = area; }

    @Override
    public boolean process(Schematic applyTo, Consumer<Schematic> replace, Level level) {
        for (final var containedIndex : Lists.reverse(applyTo.getSymbolIndicesWithin(area, level))) {
            applyTo.removeSymbol(containedIndex);
        }
        for (final var singleNetIndices : Lists.reverse(applyTo.getWiresWithin(area))) {
            applyTo.removeSegments(singleNetIndices);
        }
        return true;
    }
}
