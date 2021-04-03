package malte0811.controlengineering.logic.schematic;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.logic.cells.LeafcellType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class CellSymbol implements SchematicSymbol {
    private static final ResourceLocation SYMBOLS_SHEET = new ResourceLocation(
            ControlEngineering.MODID, "textures/gui/schematic_symbols.png"
    );
    private static final float TEXT_SCALE = 4;
    private final LeafcellType<?> type;
    private final SubTexture texture;

    public CellSymbol(LeafcellType<?> type, int uMin, int vMin, int uSize, int vSize) {
        this.type = type;
        this.texture = new SubTexture(SYMBOLS_SHEET, uMin, vMin, uMin + uSize, vMin + vSize);
    }

    @Override
    public void render(MatrixStack transform, int x, int y) {
        transform.push();
        transform.translate(x, y, 0);
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        texture.blit(transform, 0, getTotalFontHeight());
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
    public boolean allowIntersecting() {
        return false;
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
}
