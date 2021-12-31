package malte0811.controlengineering.gui.tape;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.client.render.target.QuadBuilder;
import malte0811.controlengineering.gui.ScreenUtils;
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

import static net.minecraft.client.gui.GuiComponent.blit;
import static net.minecraft.client.gui.GuiComponent.fill;

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

    public void render(PoseStack matrixStack) {
        byte[] shownBytes = data;
        matrixStack.pushPose();
        matrixStack.translate(xStart, 0, 0);
        renderHoles(matrixStack, shownBytes);
        matrixStack.translate(-1.5, 0, 0);
        renderChars(matrixStack, shownBytes);
        renderRSAndColor(matrixStack, shownBytes);
        matrixStack.popPose();
    }

    public void setData(byte[] data) {
        Preconditions.checkState(data.length == this.data.length);
        this.data = data;
    }

    private void renderHoles(PoseStack matrixStack, byte[] shownBytes) {
        forEachRow(matrixStack, shownBytes, CHAR_DISTANCE, yStart, (transform, currentByte) -> {
            for (int bit = 0; bit < HOLE_OFFSETS.length; ++bit) {
                if (!BitUtils.getBit(currentByte, bit)) {
                    int yPos = HOLE_OFFSETS[bit];
                    fill(transform, 0, yPos, HOLE_WIDTH, yPos + HOLE_HEIGHT, TAPE_COLOR);
                }
            }
        });
    }

    private void renderChars(PoseStack matrixStack, byte[] shownBytes) {
        double vOffset = yStart + TAPE_WIDTH + 1;
        final int delta = 8;
        forEachRow(matrixStack, shownBytes, delta, vOffset, (transform, currentByte) -> {
                    char asChar = (char) BitUtils.clearParity(currentByte);
                    if (asChar <= ' ' || asChar >= 0x7f) {
                        asChar = '.';
                    }
                    String toPrint = String.valueOf(asChar);
                    float width = font.get().getSplitter().stringWidth(toPrint);
                    float centerOffset = (delta - width) / 2f;
                    int color = -1;
                    if (!BitUtils.isCorrectParity(currentByte)) {
                        color &= 0xff_00_00;
                    }
                    font.get().draw(transform, toPrint, centerOffset, 0, color);
                }
        );
    }

    private void renderRSAndColor(PoseStack matrixStack, byte[] shownBytes) {
        final int sideSpace = -2;
        final double vOffset = yStart + TAPE_WIDTH + font.get().lineHeight - 2;
        final float rsSize = 16 + 2 * sideSpace;
        TextureAtlas texture = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        TextureAtlasSprite sprite = texture.getSprite(new ResourceLocation("block/redstone_dust_dot"));
        ScreenUtils.bindForShader(sprite);
        forEachRow(matrixStack, shownBytes, rsSize, vOffset, (transform, currentByte) -> {
            int strength = RedstoneTapeUtils.getStrength(currentByte);
            int color = RedStoneWireBlock.getColorForPower(strength);
            blitWithColor(transform, sideSpace, 16, 16, sprite, color);
        });

        TextureAtlasSprite white = QuadBuilder.getWhiteTexture();
        ScreenUtils.bindForShader(white);
        forEachRow(matrixStack, shownBytes, 1, vOffset + CHAR_DISTANCE + 2, (transform, currentByte) -> {
            final DyeColor color = RedstoneTapeUtils.getColor(currentByte);
            blitWithColor(transform, 0, 1, 1, white, color.getTextColor());
        });
    }

    private void forEachRow(
            PoseStack matrixStack,
            byte[] shownBytes,
            float width,
            double verticalOffset, BiConsumer<PoseStack, Byte> draw
    ) {
        matrixStack.pushPose();
        matrixStack.translate(0, verticalOffset, 0);
        float factor = CHAR_DISTANCE / width;
        matrixStack.scale(factor, factor, 1);
        for (byte b : shownBytes) {
            draw.accept(matrixStack, b);
            matrixStack.translate(width, 0, 0);
        }
        matrixStack.popPose();
    }

    private static void blitWithColor(
            PoseStack m, int x, int width, int height, TextureAtlasSprite texture, int color
    ) {
        RenderSystem.setShaderColor(
                BitUtils.getBits(color, 16, 8) / 255f,
                BitUtils.getBits(color, 8, 8) / 255f,
                BitUtils.getBits(color, 0, 8) / 255f,
                1
        );
        blit(m, x, 0, 0, width, height, texture);
    }
}
