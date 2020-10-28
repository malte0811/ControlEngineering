package malte0811.controlengineering.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.renders.RenderHelper;
import malte0811.controlengineering.render.utils.TransformingVertexBuilder;
import malte0811.controlengineering.util.BitUtils;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
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
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;

public class ViewTapeScreen extends Screen {
    public static final ResourceLocation BASE_SCREEN = new ResourceLocation(
            ControlEngineering.MODID,
            "textures/gui/read_tape.png"
    );
    //TODO
    private static final int WIDTH = 256;
    private static final int HEIGHT = 128;
    private static final int TAPE_COLOR = 0xffcea1a2;
    private static final int FIRST_CHAR_X = 48;
    private static final int FIRST_HOLE_Y = 65;
    private static final int HOLE_WIDTH = 3;
    private static final int HOLE_HEIGHT = 2;
    private static final int CHAR_DISTANCE = HOLE_WIDTH + 3;
    private static final int NUM_VISIBLE_CHARS = 27;
    final int TAPE_WIDTH = 29;
    private static final int[] HOLE_OFFSETS = {
            0, 3, 6,
            11, 14, 17, 20, 23
    };
    private final byte[] data;
    private int offset = 0;

    public ViewTapeScreen(String titleIn, byte[] data) {
        super(new StringTextComponent(titleIn));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        final int buttonWidth = 20;
        final int leftButtonX = FIRST_CHAR_X - 30 + (this.width - WIDTH) / 2;
        final int buttonY = FIRST_HOLE_Y + 3 + (this.height - HEIGHT) / 2;
        addButton(new Button(
                leftButtonX + 1, buttonY, buttonWidth, 20,
                new StringTextComponent("<"),
                btn -> {
                    if (offset < Math.max(data.length - NUM_VISIBLE_CHARS / 2, 0)) {
                        ++offset;
                    }
                }
        ));
        addButton(new Button(
                width - leftButtonX - buttonWidth + 1, buttonY, buttonWidth, 20,
                new StringTextComponent(">"),
                btn -> {
                    if (offset > -NUM_VISIBLE_CHARS / 2) {
                        --offset;
                    }
                }
        ));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(BASE_SCREEN);
        {
            int startX = (this.width - WIDTH) / 2;
            int startY = (this.height - HEIGHT) / 2;
            matrixStack.push();
            matrixStack.translate(startX, startY, 0);
        }
        blit(matrixStack, 0, 0, 0, 0, WIDTH, HEIGHT);
        byte[] shownBytes = getShownBytes();
        matrixStack.translate(FIRST_CHAR_X, 0, 0);
        renderHoles(matrixStack, shownBytes);
        renderChars(matrixStack, shownBytes);
        renderRSAndColor(matrixStack, shownBytes);
        matrixStack.pop();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private void renderHoles(MatrixStack matrixStack, byte[] shownBytes) {
        forEachRow(matrixStack, shownBytes, CHAR_DISTANCE, FIRST_HOLE_Y, (transform, currentByte) -> {
            for (int bit = 0; bit < HOLE_OFFSETS.length; ++bit) {
                if (!BitUtils.getBit(currentByte, bit)) {
                    int yPos = HOLE_OFFSETS[bit];
                    fill(transform, 0, yPos, HOLE_WIDTH, yPos + HOLE_HEIGHT, TAPE_COLOR);
                }
            }
        });
    }

    private void renderChars(MatrixStack matrixStack, byte[] shownBytes) {
        double vOffset = FIRST_HOLE_Y + TAPE_WIDTH + 1 + font.FONT_HEIGHT / 2.;
        forEachRow(matrixStack, shownBytes, 8, vOffset, (transform, currentByte) -> {
                    char asChar = (char) BitUtils.clearParity(currentByte);
                    if (asChar <= ' ' || asChar >= 0x7f) {
                        asChar = '.';
                    }
                    String toPrint = String.valueOf(asChar);
                    int width = font.getStringWidth(toPrint);
                    int distToNext = CHAR_DISTANCE * 2;
                    int centerOffset = (distToNext - width) / 2;
                    int color = -1;
                    if (!BitUtils.isCorrectParity(currentByte)) {
                        color &= 0xff_00_00;
                    }
                    font.drawString(transform, toPrint, centerOffset, 0, color);
                }
        );
    }

    private void renderRSAndColor(MatrixStack matrixStack, byte[] shownBytes) {
        final int sideSpace = -2;
        final double vOffset = FIRST_HOLE_Y + TAPE_WIDTH + font.FONT_HEIGHT + 2;
        final float rsSize = 16 + 2 * sideSpace;
        AtlasTexture texture = getMinecraft().getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        texture.bindTexture();
        TextureAtlasSprite sprite = texture.getSprite(new ResourceLocation("block/redstone_dust_dot"));
        forEachRow(matrixStack, shownBytes, rsSize, vOffset, (transform, currentByte) -> {
            int strength = RedstoneTapeUtils.getStrength(currentByte);
            int color = RedstoneWireBlock.getRGBByPower(strength);
            blitWithColor(transform, sideSpace, 0, 16, 16, sprite, color);
        });

        TextureAtlasSprite white = RenderHelper.getWhiteTexture();
        forEachRow(matrixStack, shownBytes, 1, vOffset + CHAR_DISTANCE + 2, (transform, currentByte) -> {
            final DyeColor color = RedstoneTapeUtils.getColor(currentByte);
            blitWithColor(transform, 0, 0, 1, 1, white, color.getColorValue());
        });
    }

    private byte[] getShownBytes() {
        byte[] result = new byte[NUM_VISIBLE_CHARS];
        for (int i = 0; i < NUM_VISIBLE_CHARS; ++i) {
            int actualIndex = i + offset;
            if (actualIndex < data.length && actualIndex >= 0) {
                result[i] = data[actualIndex];
            } else {
                result[i] = 0;
            }
        }
        return result;
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static void blitWithColor(
            MatrixStack m, int x, int y, int width, int height, TextureAtlasSprite texture, int color
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
        inner.pos(matrix, x, y + height, 0).tex(minU, maxV).endVertex();
        inner.pos(matrix, x + width, y + height, 0).tex(maxU, maxV).endVertex();
        inner.pos(matrix, x + width, y, 0).tex(maxU, minV).endVertex();
        inner.pos(matrix, x, y, 0).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
