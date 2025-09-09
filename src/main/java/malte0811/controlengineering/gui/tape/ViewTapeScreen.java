package malte0811.controlengineering.gui.tape;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.client.render.utils.ScreenUtils;
import malte0811.controlengineering.network.CutTapePacket;
import malte0811.controlengineering.util.RLUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ViewTapeScreen extends Screen {
    public static final ResourceLocation BASE_SCREEN = RLUtils.ceLoc("textures/gui/read_tape.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 128;
    private static final int FIRST_CHAR_X = 48;
    private static final int FIRST_HOLE_Y = 65;
    private static final int TAPE_MIN_Y = FIRST_HOLE_Y - 2;
    private static final int NUM_VISIBLE_CHARS = 27;
    private final ByteList fullData;
    private int offset = 0;
    private final TapeRender tapeRender;
    private final boolean canCut;
    private final InteractionHand tapeHand;

    public ViewTapeScreen(String titleIn, ByteList data, InteractionHand tapeHand) {
        super(Component.literal(titleIn));
        this.fullData = data;
        this.canCut = CutTapePacket.canCut(tapeHand, Objects.requireNonNull(Minecraft.getInstance().player));
        this.tapeHand = tapeHand;
        this.tapeRender = new TapeRender(FIRST_CHAR_X, FIRST_HOLE_Y, () -> font, getShownBytes());
    }

    @Override
    protected void init() {
        super.init();
        final int buttonWidth = 20;
        final int leftButtonX = FIRST_CHAR_X - 30 + (this.width - WIDTH) / 2;
        final int buttonY = FIRST_HOLE_Y + 3 + (this.height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.literal("<"), btn -> incOffset())
                .pos(leftButtonX + 1, buttonY)
                .size(buttonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), btn -> decOffset())
                .pos(width - leftButtonX - buttonWidth + 1, buttonY)
                .size(buttonWidth, 20)
                .build());
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BASE_SCREEN);
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(startX, startY, 0);
        graphics.blit(BASE_SCREEN, 0, 0, 0, 0, WIDTH, HEIGHT);
        tapeRender.render(graphics);
        if (canCut) {
            final int visualCutOffset = getVisualFocussedRow(mouseX - startX, mouseY - startY);
            if (visualCutOffset >= 0) {
                final double xMin = FIRST_CHAR_X + (visualCutOffset) * TapeRender.CHAR_DISTANCE - 1.5;
                final int color = 0x80_ff0000;
                ScreenUtils.fill(
                        graphics.pose(),
                        xMin, TAPE_MIN_Y, xMin + TapeRender.CHAR_DISTANCE, TAPE_MIN_Y + TapeRender.TAPE_WIDTH,
                        color
                );
            }
        }
        graphics.pose().popPose();

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private int getVisualFocussedRow(double mouseX, double mouseY) {
        if (mouseY < TAPE_MIN_Y || mouseY > TAPE_MIN_Y + TapeRender.TAPE_WIDTH) {
            return -1;
        }
        int row = (int) Math.round((mouseX - FIRST_CHAR_X - 1.5) / TapeRender.CHAR_DISTANCE);
        if (row < 0 || row < -offset || row >= NUM_VISIBLE_CHARS || row >= fullData.size() - offset) {
            return -1;
        }
        return row;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta, double unknown) {
        if (delta < 0) {
            incOffset();
        } else {
            decOffset();
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = (this.width - WIDTH) / 2;
        int startY = (this.height - HEIGHT) / 2;
        int visualRow = getVisualFocussedRow(mouseX - startX, mouseY - startY);
        if (canCut && visualRow >= 0) {
            onClose();
            PacketDistributor.sendToServer(new CutTapePacket(tapeHand, visualRow + offset));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private byte[] getShownBytes() {
        byte[] result = new byte[NUM_VISIBLE_CHARS];
        for (int i = 0; i < NUM_VISIBLE_CHARS; ++i) {
            int actualIndex = i + offset;
            if (actualIndex < fullData.size() && actualIndex >= 0) {
                result[i] = fullData.getByte(actualIndex);
            } else {
                result[i] = 0;
            }
        }
        return result;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void incOffset() {
        if (offset < Math.max(fullData.size() - NUM_VISIBLE_CHARS / 2, 0)) {
            ++offset;
        }
        tapeRender.setData(getShownBytes());
    }

    private void decOffset() {
        if (offset > -NUM_VISIBLE_CHARS / 2) {
            --offset;
        }
        tapeRender.setData(getShownBytes());
    }
}
