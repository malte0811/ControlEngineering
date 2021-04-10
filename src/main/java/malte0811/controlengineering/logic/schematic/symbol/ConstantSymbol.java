package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.widgets.IntSelector;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import malte0811.controlengineering.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ConstantSymbol extends SchematicSymbol<Double> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.analogStrength";
    public static final String NAME = ControlEngineering.MODID + ".gui.constantSymbol";
    private static final int BOX_SIZE = 5;

    private static final List<SymbolPin> DIGITAL = ImmutableList.of(SymbolPin.digitalOut(BOX_SIZE + 2, BOX_SIZE / 2));
    private static final List<SymbolPin> ANALOG = ImmutableList.of(SymbolPin.analogOut(BOX_SIZE + 2, BOX_SIZE / 2));

    public ConstantSymbol() {
        super(0., Codec.DOUBLE);
    }

    @Override
    public void renderCustom(MatrixStack transform, int x, int y, @Nullable Double state) {
        int color = RedstoneTapeUtils.getRSColor(state == null ? 0 : state.floatValue());
        AbstractGui.fill(
                transform,
                x + BOX_SIZE, y + BOX_SIZE / 2,
                x + BOX_SIZE + 1, y + BOX_SIZE / 2 + 1,
                color
        );
        final String text;
        if (state != null) {
            text = Integer.toString((int) Math.round(state * BusLine.MAX_VALID_VALUE));
        } else {
            text = "";
        }
        TextUtil.renderBoxWithText(transform, color, text, 4, x, y, BOX_SIZE, BOX_SIZE);
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
    public List<SymbolPin> getPins(@Nullable Double state) {
        if (state == null || (state != 0 && state != 1)) {
            return ANALOG;
        } else {
            return DIGITAL;
        }
    }

    @Override
    public void createInstanceWithUI(Consumer<? super SymbolInstance<Double>> onDone) {
        Minecraft.getInstance().displayGuiScreen(new IntSelector(
                i -> onDone.accept(new SymbolInstance<>(this, i / (double) BusLine.MAX_VALID_VALUE)),
                INPUT_KEY
        ));
    }

    @Override
    public ITextComponent getName() {
        return new TranslationTextComponent(NAME);
    }

    @Override
    public List<IFormattableTextComponent> getExtraDescription(Double state) {
        return ImmutableList.of(new TranslationTextComponent(INPUT_KEY, (int) (state * BusLine.MAX_VALID_VALUE)));
    }
}
