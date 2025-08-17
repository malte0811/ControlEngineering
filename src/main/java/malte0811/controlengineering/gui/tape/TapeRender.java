package malte0811.controlengineering.gui.tape;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.RedStoneWireBlock;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;

public class TapeRender {
    private static final int TAPE_COLOR = 0xffcea1a2;
    private static final int HOLE_WIDTH = 3;
    private static final int HOLE_HEIGHT = 2;
    public static final int CHAR_DISTANCE = HOLE_WIDTH + 3;
    public static final int TAPE_WIDTH = 29;
    private static final int[] HOLE_OFFSETS = {
            0, 3, 6,
            11, 14, 17, 20, 23
    };

    private final int xStart;
    private final int yStart;
    private final Supplier<Font> font;

    private byte[] data;

    public TapeRender(int xStart, int yStart, Supplier<Font> font, byte[] data) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.font = font;
        this.data = data;
    }

    public void render(GuiGraphics graphics) {
        byte[] shownBytes = data;
        graphics.pose().pushPose();
        graphics.pose().translate(xStart, 0, 0);
        renderHoles(graphics, shownBytes);
        graphics.pose().translate(-1.5, 0, 0);
        renderChars(graphics, shownBytes);
        renderRSAndColor(graphics, shownBytes);
        graphics.pose().popPose();
    }

    public void setData(byte[] data) {
        Preconditions.checkState(data.length == this.data.length);
        this.data = data;
    }

    private void renderHoles(GuiGraphics graphics, byte[] shownBytes) {
        forEachRow(graphics, shownBytes, CHAR_DISTANCE, yStart, (transform, currentByte) -> {
            for (int bit = 0; bit < HOLE_OFFSETS.length; ++bit) {
                if (!BitUtils.getBit(currentByte, bit)) {
                    int yPos = HOLE_OFFSETS[bit];
                    graphics.fill(0, yPos, HOLE_WIDTH, yPos + HOLE_HEIGHT, TAPE_COLOR);
                }
            }
        });
    }

    private void renderChars(GuiGraphics graphics, byte[] shownBytes) {
        double vOffset = yStart + TAPE_WIDTH + 1;
        final int delta = 8;
        forEachRow(graphics, shownBytes, delta, vOffset, (transform, currentByte) -> {
                    char asChar = (char) BitUtils.clearParity(currentByte);
                    if (asChar <= ' ' || asChar >= 0x7f) {
                        asChar = '.';
                    }
                    String toPrint = String.valueOf(asChar);
                    float width = font.get().getSplitter().stringWidth(toPrint);
                    int centerOffset = (int) ((delta - width) / 2f);
                    int color = -1;
                    if (!BitUtils.isCorrectParity(currentByte)) {
                        color &= 0xff_00_00;
                    }
                    graphics.drawString(this.font.get(), toPrint, centerOffset, 0, color);
                }
        );
    }

    private void renderRSAndColor(GuiGraphics graphics, byte[] shownBytes) {
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        final int sideSpace = -2;
        final double vOffset = yStart + TAPE_WIDTH + font.get().lineHeight - 2;
        final float rsSize = 16 + 2 * sideSpace;
        TextureAtlas texture = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite sprite = texture.getSprite(new ResourceLocation("block/redstone_dust_dot"));
        forEachRow(graphics, shownBytes, rsSize, vOffset, (transform, currentByte) -> {
            int strength = RedstoneTapeUtils.getStrength(currentByte);
            int color = RedStoneWireBlock.getColorForPower(strength);
            blitWithColor(graphics, sideSpace, 16, 16, sprite, color);
        });

        TextureAtlasSprite white = QuadBuilder.getWhiteTexture();
        forEachRow(graphics, shownBytes, 1, vOffset + CHAR_DISTANCE + 2, (transform, currentByte) -> {
            final DyeColor color = RedstoneTapeUtils.getColor(currentByte);
            blitWithColor(graphics, 0, 1, 1, white, color.getTextColor());
        });
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private void forEachRow(
            GuiGraphics graphics,
            byte[] shownBytes,
            float width,
            double verticalOffset, BiConsumer<PoseStack, Byte> draw
    ) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, verticalOffset, 0);
        float factor = CHAR_DISTANCE / width;
        graphics.pose().scale(factor, factor, 1);
        for (byte b : shownBytes) {
            draw.accept(graphics.pose(), b);
            graphics.pose().translate(width, 0, 0);
        }
        graphics.pose().popPose();
    }

    private static void blitWithColor(
            GuiGraphics graphics,
            int x, int width, int height, TextureAtlasSprite texture, int color
    ) {
        RenderSystem.setShaderColor(
                BitUtils.getBits(color, 16, 8) / 255f,
                BitUtils.getBits(color, 8, 8) / 255f,
                BitUtils.getBits(color, 0, 8) / 255f,
                1
        );

        graphics.blit(x, 0, 0, width, height, texture);
    }
}
