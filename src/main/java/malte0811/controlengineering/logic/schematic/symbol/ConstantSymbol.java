package malte0811.controlengineering.logic.schematic.symbol;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusLine;
import malte0811.controlengineering.gui.widgets.IntSelector;
import malte0811.controlengineering.util.RedstoneTapeUtils;
import malte0811.controlengineering.util.TextUtil;
import malte0811.controlengineering.util.serialization.mycodec.MyCodecs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ConstantSymbol extends SchematicSymbol<Double> {
    public static final String INPUT_KEY = ControlEngineering.MODID + ".gui.analogStrength";
    public static final String NAME = ControlEngineering.MODID + ".gui.constantSymbol";
    private static final int BOX_SIZE = 5;

    private static final List<SymbolPin> DIGITAL = ImmutableList.of(SymbolPin.digitalOut(
            BOX_SIZE + 2,
            BOX_SIZE / 2,
            "out"
    ));
    private static final List<SymbolPin> ANALOG = ImmutableList.of(SymbolPin.analogOut(
            BOX_SIZE + 2,
            BOX_SIZE / 2,
            "out"
    ));

    public ConstantSymbol() {
        super(0., MyCodecs.DOUBLE);
    }

    @Override
    public void renderCustom(PoseStack transform, int x, int y, @Nullable Double state) {
        int color = RedstoneTapeUtils.getRSColor(state == null ? 0 : state.floatValue());
        GuiComponent.fill(
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
        Minecraft.getInstance().setScreen(new IntSelector(
                i -> onDone.accept(new SymbolInstance<>(this, i / (double) BusLine.MAX_VALID_VALUE)),
                INPUT_KEY
        ));
    }

    @Override
    public Component getName() {
        return new TranslatableComponent(NAME);
    }

    @Override
    public List<MutableComponent> getExtraDescription(Double state) {
        return ImmutableList.of(new TranslatableComponent(INPUT_KEY, (int) (state * BusLine.MAX_VALID_VALUE)));
    }
}
