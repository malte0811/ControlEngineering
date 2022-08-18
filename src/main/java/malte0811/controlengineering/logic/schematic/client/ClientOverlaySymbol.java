package malte0811.controlengineering.logic.schematic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.logic.schematic.symbol.SchematicSymbol;
import malte0811.controlengineering.logic.schematic.symbol.SymbolInstance;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class ClientOverlaySymbol<State, Symbol extends SchematicSymbol<State>> extends ClientSymbol<State, Symbol> {
    private final ClientSymbol<State, Symbol> original;
    private final String overlay;
    private final float xOff;
    private final float yOff;

    protected ClientOverlaySymbol(ClientSymbol<State, Symbol> original, String overlay, float xOff, float yOff) {
        super(original.serverSymbol);
        this.original = original;
        this.overlay = overlay;
        this.xOff = xOff;
        this.yOff = yOff;
    }

    @Override
    protected void renderCustom(PoseStack transform, int x, int y, State state, int alpha) {
        original.renderCustom(transform, x, y, state, alpha);
        transform.pushPose();
        transform.translate(x + xOff + 0.5, y + yOff, 0);
        transform.scale(1 / 6f, 1 / 6f, 1);
        final var font = Minecraft.getInstance().font;
        final var width = font.width(overlay) - 1;
        font.draw(transform, overlay, -width / 2f, 0, alpha << 24);
        transform.popPose();
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<State>> onDone, State initialState) {
        original.createInstanceWithUI(onDone, initialState);
    }
}
