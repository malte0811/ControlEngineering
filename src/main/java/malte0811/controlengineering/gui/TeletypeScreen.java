package malte0811.controlengineering.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.widgets.KeyboardButton;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

public class TeletypeScreen extends Screen {
    private static final int KEY_SIZE = 20;
    private static final int NUM_VISIBLE_CHARS = 23;
    private static final int MAX_CHARS_PER_SECOND = 8;
    private static final int MIN_CHAR_DELAY = 1000 / MAX_CHARS_PER_SECOND;
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ControlEngineering.MODID,
            "textures/gui/teletype.png"
    );
    private static final SubTexture MAIN_SCREEN = new SubTexture(TEXTURE, 0, 0, 256, 79);
    private static final SubTexture SMALL_KEY = new SubTexture(TEXTURE, 0, 128, 16, 144);
    private static final SubTexture CAPS_KEY = new SubTexture(TEXTURE, 0, 144, 32, 160);
    private static final SubTexture SPACE_KEY = new SubTexture(TEXTURE, 0, 160, 128, 176);

    private final TapeRender tapeRender;
    //TODO should go directly into the tile
    private final ByteList typedChars = new ByteArrayList();
    private final ByteList pending = new ByteArrayList();
    private boolean isCapsLock;
    private long lastTyped = System.currentTimeMillis();

    public TeletypeScreen() {
        super(new TranslationTextComponent("screen.controlengineering.teletype"));
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
                addButton(new KeyboardButton(
                        x,
                        y,
                        c -> typeOrBuffer((byte) c),
                        SMALL_KEY,
                        rowChars[col],
                        () -> isCapsLock
                ));
            }
        }
        addButton(CAPS_KEY.createButton(
                getKeyX(Keyboard.CAPSLOCK_START), getRowY(Keyboard.CAPSLOCK_ROW), btn -> isCapsLock = !isCapsLock
        ));
        addButton(SPACE_KEY.createButton(
                (this.width - SPACE_KEY.getWidth()) / 2, getRowY(Keyboard.SPACE_ROW), btn -> typeOrBuffer((byte) ' ')
        ));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        {
            matrixStack.push();
            matrixStack.translate(getXStart(), getYStart(), 0);
        }
        MAIN_SCREEN.blit(matrixStack, 0, 0);
        tapeRender.render(matrixStack);
        matrixStack.pop();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (canTypeNextChar() && !pending.isEmpty()) {
            final byte next = pending.removeByte(0);
            type(next);
        }
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
        int shownStart = Math.max(0, NUM_VISIBLE_CHARS - typedChars.size());
        int typedStart = Math.max(0, typedChars.size() - NUM_VISIBLE_CHARS);
        for (int i = 0; i < shown.length - shownStart; ++i) {
            shown[shownStart + i] = typedChars.getByte(typedStart + i);
        }
        return shown;
    }

    private void typeOrBuffer(byte newChar) {
        if (canTypeNextChar()) {
            type(newChar);
        } else if (pending.size() < MAX_CHARS_PER_SECOND / 2) {
            pending.add(newChar);
        }
    }

    private void type(byte newChar) {
        long now = System.currentTimeMillis();
        if (now - lastTyped < 2 * MIN_CHAR_DELAY) {
            lastTyped += MIN_CHAR_DELAY;
        } else {
            lastTyped = now;
        }
        typedChars.add(BitUtils.fixParity(newChar));
        tapeRender.setData(getBytes());
    }

    private boolean canTypeNextChar() {
        return System.currentTimeMillis() > lastTyped + MIN_CHAR_DELAY;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint <= 127/* && */) {
            typeOrBuffer((byte) codePoint);
            return true;
        } else {
            return false;
        }
    }
}
