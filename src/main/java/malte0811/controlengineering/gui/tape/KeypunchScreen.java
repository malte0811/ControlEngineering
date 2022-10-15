package malte0811.controlengineering.gui.tape;

import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blockentity.tape.KeypunchState;
import malte0811.controlengineering.gui.SubTexture;
import malte0811.controlengineering.gui.widget.Keyboard;
import malte0811.controlengineering.gui.widget.KeyboardButton;
import malte0811.controlengineering.network.keypunch.Backspace;
import malte0811.controlengineering.network.keypunch.KeypunchPacket;
import malte0811.controlengineering.network.keypunch.KeypunchSubPacket;
import malte0811.controlengineering.network.keypunch.TypeChar;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

public class KeypunchScreen extends Screen implements MenuAccess<KeypunchMenu> {
    private static final int KEY_SIZE = 20;
    private static final int NUM_VISIBLE_CHARS = 23;
    // TODO review once a sound exists
    private static final int MAX_CHARS_PER_SECOND = 8;
    private static final int MIN_CHAR_DELAY = 1000 / MAX_CHARS_PER_SECOND;
    public static final ResourceLocation TEXTURE = ControlEngineering.ceLoc("textures/gui/keypunch.png");
    private static final SubTexture MAIN_SCREEN = new SubTexture(TEXTURE, 0, 0, 256, 79);
    private static final SubTexture SMALL_KEY = new SubTexture(TEXTURE, 0, 128, 16, 144);
    private static final SubTexture CAPS_KEY = new SubTexture(TEXTURE, 0, 144, 32, 160);
    private static final SubTexture SPACE_KEY = new SubTexture(TEXTURE, 0, 160, 128, 176);

    private final KeypunchMenu container;
    private final KeypunchState state;
    private final TapeRender tapeRender;

    private boolean isCapsLock;

    public KeypunchScreen(KeypunchMenu container, Component title) {
        super(title);
        this.container = container;
        this.state = container.getState();
        this.tapeRender = new TapeRender(50, 27, () -> font, getBytes());
    }

    @Override
    protected void init() {
        super.init();

        for (int row = 0; row < Keyboard.ROWS.length; ++row) {
            final int y = getRowY(row);
            final char[] rowChars = Keyboard.ROWS[row].chars.toCharArray();
            for (int col = 0; col < rowChars.length; col++) {
                final int x = getKeyX(Keyboard.ROWS[row].relativeStartOffset + col);
                addRenderableWidget(new KeyboardButton(
                        x, y, c -> type((byte) c), SMALL_KEY, rowChars[col], () -> isCapsLock
                ));
            }
        }
        addRenderableWidget(CAPS_KEY.createButton(
                getKeyX(Keyboard.CAPSLOCK_START), getRowY(Keyboard.CAPSLOCK_ROW), btn -> isCapsLock = !isCapsLock
        ));
        addRenderableWidget(SPACE_KEY.createButton(
                (this.width - SPACE_KEY.getWidth()) / 2, getRowY(Keyboard.SPACE_ROW), btn -> type((byte) ' ')
        ));
    }

    @Override
    public void render(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        {
            matrixStack.pushPose();
            matrixStack.translate(getXStart(), getYStart(), 0);
        }
        MAIN_SCREEN.blit(matrixStack, 0, 0);
        tapeRender.render(matrixStack);
        font.draw(matrixStack, Integer.toString(state.getAvailable()), 210, 35, -1);
        matrixStack.popPose();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getRowY(int row) {
        return getYStart() + 85 + row * KEY_SIZE;
    }

    private int getKeyX(double relative) {
        return (int) (getXStart() + relative * KEY_SIZE);
    }

    private int getXStart() {
        return (this.width - MAIN_SCREEN.getWidth()) / 2;
    }

    private int getYStart() {
        return (this.height - MAIN_SCREEN.getHeight() - 5 * KEY_SIZE) / 2;
    }

    private byte[] getBytes() {
        byte[] shown = new byte[NUM_VISIBLE_CHARS];
        final int numToCopy = Math.min(NUM_VISIBLE_CHARS, state.getData().size());
        final int firstArrayIndex = Math.max(0, NUM_VISIBLE_CHARS - numToCopy);
        final int firstListIndex = Math.max(0, state.getData().size() - numToCopy);
        for (int i = 0; i < numToCopy; ++i) {
            shown[i + firstArrayIndex] = state.getData().getByte(i + firstListIndex);
        }
        return shown;
    }

    private void type(byte newChar) {
        processAndSend(new TypeChar(newChar));
    }

    public void updateData() {
        tapeRender.setData(getBytes());
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint <= 127) {
            type((byte) codePoint);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            return processAndSend(new Backspace());
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Nonnull
    @Override
    public KeypunchMenu getMenu() {
        return container;
    }

    public KeypunchState getState() {
        return state;
    }

    private boolean processAndSend(KeypunchSubPacket packet) {
        if (!container.isLoopback() || packet.process(state)) {
            updateData();
            ControlEngineering.NETWORK.sendToServer(new KeypunchPacket(packet));
            return true;
        } else {
            return false;
        }
    }
}
