package malte0811.controlengineering.gui.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Function3;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.gui.StackedScreen;
import malte0811.controlengineering.gui.widget.BasicSlider;
import malte0811.controlengineering.gui.widget.ColorSelector;
import malte0811.controlengineering.gui.widget.FractionSelector;
import malte0811.controlengineering.logic.cells.impl.InvertingAmplifier;
import malte0811.controlengineering.logic.cells.impl.VoltageDivider;
import malte0811.controlengineering.util.math.Fraction;
import malte0811.controlengineering.util.mycodec.MyCodec;
import malte0811.controlengineering.util.mycodec.MyCodecs;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DataProviderScreen<T> extends StackedScreen {
    public static final String DONE_KEY = ControlEngineering.MODID + ".gui.done";
    private static final Map<MyCodec<?>, DataProviderWidget.Factory<?, ?>> KNOWN_FACTORIES = new HashMap<>();

    // Workaround for generics issues
    private static <T, W extends AbstractWidget & IDataProviderWidget<T>>
    void registerFactoryFunc3(MyCodec<T> type, Function3<T, Integer, Integer, W> factory) {
        registerFactory(type, new IDataProviderWidget.Factory<T, W>() {
            @Override
            public W create(@Nullable T currentValue, int x, int y) {
                return factory.apply(currentValue, x, y);
            }
        });
    }

    private static <T> void registerFactory(MyCodec<T> type, IDataProviderWidget.Factory<T, ?> factory) {
        KNOWN_FACTORIES.put(type, factory);
    }

    static {
        registerFactoryFunc3(BusSignalRef.CODEC, BusSignalSelector::new);
        registerFactoryFunc3(ColorAndSignal.CODEC, ColorAndSignalWidget::new);
        registerFactoryFunc3(ColorAndText.CODEC, ColorAndTextWidget::new);
        registerFactoryFunc3(MyCodecs.HEX_COLOR, ColorSelector::new);
        registerFactoryFunc3(MyCodecs.STRING, TextProviderWidget::arbitrary);
        registerFactory(VoltageDivider.RESISTANCE_CODEC, BasicSlider.withRange(
                0, VoltageDivider.TOTAL_RESISTANCE, VoltageDivider.RESISTANCE_KEY
        ));
        registerFactory(Fraction.CODEC, FractionSelector.with(
                255, InvertingAmplifier.AMPLIFY_BY, InvertingAmplifier.ATTENUATE_BY
        ));
    }

    @Nullable
    public static <T>
    DataProviderScreen<T> makeFor(Component title, @Nonnull T initial, MyCodec<T> type, Consumer<T> out) {
        var factory = (DataProviderWidget.Factory<T, ?>) KNOWN_FACTORIES.get(type);
        if (factory != null) {
            return new DataProviderScreen<>(title, factory, initial, out);
        } else {
            return null;
        }
    }

    private final IDataProviderWidget.Factory<T, ?> factory;
    @Nullable
    private final T initial;
    private final Consumer<T> out;
    private IDataProviderWidget<T> provider;

    public DataProviderScreen(
            Component titleIn, IDataProviderWidget.Factory<T, ?> factory, @Nullable T initial, Consumer<T> out
    ) {
        super(titleIn);
        this.factory = factory;
        this.initial = initial;
        this.out = out;
    }

    @Override
    protected void init() {
        super.init();
        var providerTemp = factory.create(initial, 0, 0);
        final int xMin = (width - providerTemp.getWidth()) / 2;
        final int yMin = (height - providerTemp.getHeight()) / 2;
        var provider = factory.create(initial, xMin, yMin);
        addRenderableWidget(provider);
        addRenderableWidget(new Button(
                xMin, yMin + provider.getHeight() + 10, provider.getWidth(), 20, Component.translatable(DONE_KEY),
                $ -> onClose()
        ));
        this.provider = provider;
    }

    @Override
    public void removed() {
        super.removed();
        T result = provider.getData();
        if (result != null) {
            out.accept(result);
        }
    }

    @Override
    protected void renderForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    }
}
