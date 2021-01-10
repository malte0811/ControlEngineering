package malte0811.controlengineering.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.controlpanels.renders.target.QuadBuilder;
import malte0811.controlengineering.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static net.minecraft.client.gui.AbstractGui.fill;

public class TapeRender {
    private static final int TAPE_COLOR = 0xffcea1a2;
    private static final int HOLE_WIDTH = 3;
    private static final int HOLE_HEIGHT = 2;
    private static final int CHAR_DISTANCE = HOLE_WIDTH + 3;
    private final int TAPE_WIDTH = 29;
    private static final int[] HOLE_OFFSETS = {
            0, 3, 6,
            11, 14, 17, 20, 23
    };

    private final int xStart;
    private final int yStart;
    private final Supplier<FontRenderer> font;

    private byte[] data;

    public TapeRender(int xStart, int yStart, Supplier<FontRenderer> font, byte[] data) {
        this.xStart = xStart;
        this.yStart = yStart;
        this.font = font;
        this.data = data;
    }

    public void render(MatrixStack matrixStack) {
        byte[] shownBytes = data;
        matrixStack.push();
        matrixStack.translate(xStart, 0, 0);
        renderHoles(matrixStack, shownBytes);
        renderChars(matrixStack, shownBytes);
        renderRSAndColor(matrixStack, shownBytes);
        matrixStack.pop();
    }

    public void setData(byte[] data) {
        Preconditions.checkState(data.length == this.data.length);
        this.data = data;
    }

    private void renderHoles(MatrixStack matrixStack, byte[] shownBytes) {
        forEachRow(matrixStack, shownBytes, CHAR_DISTANCE, yStart, (transform, currentByte) -> {
            for (int bit = 0; bit < HOLE_OFFSETS.length; ++bit) {
                if (!BitUtils.getBit(currentByte, bit)) {
                    int yPos = HOLE_OFFSETS[bit];
                    fill(transform, 0, yPos, HOLE_WIDTH, yPos + HOLE_HEIGHT, TAPE_COLOR);
                }
            }
        });
    }

    private void renderChars(MatrixStack matrixStack, byte[] shownBytes) {
        double vOffset = yStart + TAPE_WIDTH + 1 + font.get().FONT_HEIGHT / 2.;
        forEachRow(matrixStack, shownBytes, 8, vOffset, (transform, currentByte) -> {
                    char asChar = (char) BitUtils.clearParity(currentByte);
                    if (asChar <= ' ' || asChar >= 0x7f) {
                        asChar = '.';
                    }
                    String toPrint = String.valueOf(asChar);
                    int width = font.get().getStringWidth(toPrint);
                    int distToNext = CHAR_DISTANCE * 2;
                    int centerOffset = (distToNext - width) / 2;
                    int color = -1;
                    if (!BitUtils.isCorrectParity(currentByte)) {
                        color &= 0xff_00_00;
                    }
                    font.get().drawString(transform, toPrint, centerOffset, 0, color);
                }
        );
    }

    private void renderRSAndColor(MatrixStack matrixStack, byte[] shownBytes) {
        final int sideSpace = -2;
        final double vOffset = yStart + TAPE_WIDTH + font.get().FONT_HEIGHT + 2;
        final float rsSize = 16 + 2 * sideSpace;
        AtlasTexture texture = Minecraft.getInstance().getModelManager()
                .getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        texture.bindTexture();
        TextureAtlasSprite sprite = texture.getSprite(new ResourceLocation("block/redstone_dust_dot"));
        forEachRow(matrixStack, shownBytes, rsSize, vOffset, (transform, currentByte) -> {
            int strength = RedstoneTapeUtils.getStrength(currentByte);
            int color = RedstoneWireBlock.getRGBByPower(strength);
            blitWithColor(transform, sideSpace, 16, 16, sprite, color);
        });

        TextureAtlasSprite white = QuadBuilder.getWhiteTexture();
        forEachRow(matrixStack, shownBytes, 1, vOffset + CHAR_DISTANCE + 2, (transform, currentByte) -> {
            final DyeColor color = RedstoneTapeUtils.getColor(currentByte);
            blitWithColor(transform, 0, 1, 1, white, color.getColorValue());
        });
    }

    private void forEachRow(
            MatrixStack matrixStack,
            byte[] shownBytes,
            float width,
            double verticalOffset, BiConsumer<MatrixStack, Byte> draw
    ) {
        matrixStack.push();
        matrixStack.translate(0, verticalOffset, 0);
        float factor = CHAR_DISTANCE / width;
        matrixStack.scale(factor, factor, 1);
        for (byte b : shownBytes) {
            draw.accept(matrixStack, b);
            matrixStack.translate(width, 0, 0);
        }
        matrixStack.pop();
    }

    private static void blitWithColor(
            MatrixStack m, int x, int width, int height, TextureAtlasSprite texture, int color
    ) {
        Matrix4f matrix = m.getLast().getMatrix();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        TransformingVertexBuilder inner = new TransformingVertexBuilder(bufferbuilder);
        inner.setColor(color | (0xff << 24));
        final float minU = texture.getMinU();
        final float maxU = texture.getMaxU();
        final float minV = texture.getMinV();
        final float maxV = texture.getMaxV();
        inner.pos(matrix, x, height, 0).tex(minU, maxV).endVertex();
        inner.pos(matrix, x + width, height, 0).tex(maxU, maxV).endVertex();
        inner.pos(matrix, x + width, 0, 0).tex(maxU, minV).endVertex();
        inner.pos(matrix, x, 0, 0).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
