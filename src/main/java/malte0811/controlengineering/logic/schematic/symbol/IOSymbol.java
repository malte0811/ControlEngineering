package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.gui.bus.BusSignalSelector;
import malte0811.controlengineering.logic.cells.SignalType;
import malte0811.controlengineering.logic.schematic.SchematicNet;
import malte0811.controlengineering.util.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.DyeColor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class IOSymbol extends SchematicSymbol<BusSignalRef> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.inputPin";
    public static final String OUTPUT_KEY = ControlEngineering.MODID + ".gui.outputPin";

    private static final List<SymbolPin> OUTPUT_PIN = ImmutableList.of(new SymbolPin(5, 1, SignalType.ANALOG));
    private static final List<SymbolPin> INPUT_PIN = ImmutableList.of(new SymbolPin(0, 1, SignalType.ANALOG));
    private final boolean isInput;

    public IOSymbol(boolean isInput) {
        super(new BusSignalRef(0, 0), BusSignalRef.CODEC);
        this.isInput = isInput;
    }

    @Override
    public void render(MatrixStack transform, int x, int y, @Nullable BusSignalRef state) {
        int color;
        if (state != null) {
            color = DyeColor.byId(state.color).getColorValue();
        } else {
            color = 0;
        }
        color |= 0xff000000;
        final int blockX = x + (isInput ? 0 : 3);
        AbstractGui.fill(transform, blockX, y, blockX + 3, y + 3, color);
        if (isInput) {
            AbstractGui.fill(transform, x + 3, y + 1, x + 4, y + 2, color);
            AbstractGui.fill(transform, x + 4, y + 1, x + 6, y + 2, SchematicNet.WIRE_COLOR);
        } else {
            AbstractGui.fill(transform, x + 2, y + 1, x + 3, y + 2, color);
            AbstractGui.fill(transform, x, y + 1, x + 2, y + 2, SchematicNet.WIRE_COLOR);
        }
        if (state != null) {
            final String text = Integer.toString(state.line);
            final FontRenderer font = Minecraft.getInstance().fontRenderer;
            final float scale = 4;
            final float yOffset = (3 - font.FONT_HEIGHT / scale) / 2;
            final float xOffset = (3 - font.getStringWidth(text) / scale) / 2;
            transform.push();
            transform.translate(xOffset + blockX, y + yOffset, 0);
            transform.scale(1 / scale, 1 / scale, 1);
            final int textColor = 0xff000000 | ColorUtils.inverseColor(color);
            font.drawString(transform, text, 0, 0, textColor);
            transform.pop();
        }
    }

    @Override
    public int getXSize() {
        return 6;
    }

    @Override
    public int getYSize() {
        return 3;
    }

    @Override
    public List<SymbolPin> getInputPins() {
        if (isInput) {
            return ImmutableList.of();
        } else {
            return INPUT_PIN;
        }
    }

    @Override
    public List<SymbolPin> getOutputPins() {
        if (isInput) {
            return OUTPUT_PIN;
        } else {
            return ImmutableList.of();
        }
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<BusSignalRef>> onDone) {
        Minecraft.getInstance().displayGuiScreen(new BusSignalSelector(ref -> {
            SymbolInstance<BusSignalRef> instance = new SymbolInstance<>(this, ref);
            onDone.accept(instance);
        }));
    }

    @Override
    public ITextComponent getDesc() {
        return new TranslationTextComponent(isInput ? INPUT_KEY : OUTPUT_KEY);
    }
}
