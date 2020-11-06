package malte0811.controlengineering.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.widgets.KeyboardButton;
import malte0811.controlengineering.network.AddTTYData;
import malte0811.controlengineering.tiles.tape.TeletypeTile;
import malte0811.controlengineering.util.BitUtils;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.mutable.MutableInt;

import javax.annotation.Nonnull;

public class TeletypeScreen extends Screen implements IHasContainer<TeletypeContainer> {
    private static final int KEY_SIZE = 20;
    private static final int NUM_VISIBLE_CHARS = 23;
    // TODO review once a sound exists
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

    private final TeletypeContainer container;
    private final TapeRender tapeRender;
    private final ByteList unsyncedChars;

    private long lastSync = System.currentTimeMillis();
    private boolean isCapsLock;

    public TeletypeScreen(TeletypeContainer container, ITextComponent title) {
        super(title);
        this.container = container;
        unsyncedChars = new ByteArrayList();
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
                        x, y, c -> type((byte) c), SMALL_KEY, rowChars[col], () -> isCapsLock
                ));
            }
        }
        addButton(CAPS_KEY.createButton(
                getKeyX(Keyboard.CAPSLOCK_START), getRowY(Keyboard.CAPSLOCK_ROW), btn -> isCapsLock = !isCapsLock
        ));
        addButton(SPACE_KEY.createButton(
                (this.width - SPACE_KEY.getWidth()) / 2, getRowY(Keyboard.SPACE_ROW), btn -> type((byte) ' ')
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
        font.drawString(matrixStack, Integer.toString(getNumAvailableChars()), 210, 35, -1);
        matrixStack.pop();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    private int getNumAvailableChars() {
        return getContainer().getTeletype()
                .map(TeletypeTile::getRemainingBytes)
                .orElse(0) - unsyncedChars.size();
    }

    @Override
    public void tick() {
        super.tick();
        if (System.currentTimeMillis() > lastSync + 500) {
            syncChars();
        }
    }

    private void syncChars() {
        if (!unsyncedChars.isEmpty()) {
            byte[] addedData = unsyncedChars.toByteArray();
            ControlEngineering.NETWORK.sendToServer(new AddTTYData(addedData));
            container.typeAll(addedData);
            unsyncedChars.clear();
            lastSync = System.currentTimeMillis();
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        syncChars();
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

    private static void copyEnd(byte[] target, MutableInt targetLength, byte[] source) {
        final int length = Math.min(targetLength.intValue(), source.length);
        if (length > 0) {
            final int targetStart = targetLength.intValue() - length;
            final int sourceStart = source.length - length;
            System.arraycopy(source, sourceStart, target, targetStart, length);
            targetLength.subtract(length);
        }
    }

    private byte[] getBytes() {
        byte[] shown = new byte[NUM_VISIBLE_CHARS];
        MutableInt targetLength = new MutableInt(NUM_VISIBLE_CHARS);
        copyEnd(shown, targetLength, unsyncedChars.toByteArray());
        container.getTeletype()
                .map(TeletypeTile::getTypedBytes)
                .ifPresent(bytes -> copyEnd(shown, targetLength, bytes));
        return shown;
    }

    private void type(byte newChar) {
        if (getNumAvailableChars() > 0) {
            unsyncedChars.add(BitUtils.fixParity(newChar));
            updateData();
        }
    }

    private void updateData() {
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

    @Nonnull
    @Override
    public TeletypeContainer getContainer() {
        return container;
    }
}
