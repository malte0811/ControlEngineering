package malte0811.controlengineering.logic.schematic.symbol;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.cells.LeafcellType;
import malte0811.controlengineering.util.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public class CellSymbol extends SchematicSymbol<Unit> {
    private static final ResourceLocation SYMBOLS_SHEET = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/schematic_symbols.png"
    );
    private static final float TEXT_SCALE = 4;
    private final LeafcellType<?> type;
    private final SubTexture texture;
    private final List<Vec2i> inputPins;
    private final List<Vec2i> outputPins;

    public CellSymbol(
            LeafcellType<?> type,
            int uMin, int vMin, int uSize, int vSize,
            List<Vec2i> inputPins, List<Vec2i> outputPins
    ) {
        super(Unit.INSTANCE, Codec.unit(Unit.INSTANCE));
        this.type = type;
        this.inputPins = inputPins;
        this.outputPins = outputPins;
        this.texture = new SubTexture(SYMBOLS_SHEET, uMin, vMin, uMin + uSize, vMin + vSize);
    }

    @Override
    public void render(MatrixStack transform, int x, int y, Unit state) {
        transform.push();
        final int fontOffset = getTotalFontHeight();
        transform.translate(x, y + fontOffset, 0);
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        texture.blit(transform, 0, 0);
        //TODO remove or keep in some form?
        drawPins(transform, getInputPins(), 0xff00ff00);
        drawPins(transform, getOutputPins(), 0xffff0000);
        transform.translate(0, -fontOffset, 0);
        transform.scale(1 / TEXT_SCALE, 1 / TEXT_SCALE, 1);
        final int offset = (int) ((getXSize() * TEXT_SCALE - font.getStringWidth(getTitle())) / 2);
        font.drawString(transform, getTitle(), offset, 0, 0xff000000);
        transform.pop();
    }

    @Override
    public int getXSize() {
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        return Math.max(texture.getWidth(), (int) (font.getStringWidth(getTitle()) / TEXT_SCALE));
    }

    @Override
    public int getYSize() {
        return texture.getHeight() + getTotalFontHeight();
    }

    @Override
    public List<Vec2i> getInputPins() {
        return inputPins;
    }

    @Override
    public List<Vec2i> getOutputPins() {
        return outputPins;
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<Unit>> onDone) {
        // No config required/possible
        onDone.accept(newInstance());
    }

    public static String getTranslationKey(LeafcellType<?> type) {
        return "cell." + type.getRegistryName().getNamespace() + "." + type.getRegistryName().getPath() + ".name";
    }

    private String getTitle() {
        return I18n.format(getTranslationKey(type));
    }

    private int getTotalFontHeight() {
        return (int) (Minecraft.getInstance().fontRenderer.FONT_HEIGHT / TEXT_SCALE + 1);
    }

    private void drawPins(MatrixStack stack, List<Vec2i> pinPositions, int color) {
        for (Vec2i pin : pinPositions) {
            AbstractGui.fill(stack, pin.x, pin.y, pin.x + 1, pin.y + 1, color);
        }
    }
}
