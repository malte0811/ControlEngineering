package malte0811.controlengineering.gui.misc;

import com.mojang.blaze3d.matrix.MatrixStack;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.bus.BusSignalRef;
import malte0811.controlengineering.controlpanels.components.config.ColorAndSignal;
import malte0811.controlengineering.controlpanels.components.config.ColorAndText;
import malte0811.controlengineering.gui.StackedScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DataProviderScreen<T> extends StackedScreen {
    public static final String DONE_KEY = ControlEngineering.MODID + ".gui.done";
    private static final Map<Class<?>, DataProviderWidget.Factory<?>> KNOWN_FACTORIES = new HashMap<>();

    private static <T> void registerFactory(Class<T> type, DataProviderWidget.Factory<T> factory) {
        KNOWN_FACTORIES.put(type, factory);
    }

    static {
        registerFactory(BusSignalRef.class, BusSignalSelector::new);
        registerFactory(ColorAndSignal.class, ColorAndSignalWidget::new);
        registerFactory(ColorAndText.class, ColorAndTextWidget::new);
    }

    @Nullable
    public static <T>
    DataProviderScreen<T> makeFor(ITextComponent title, @Nonnull T initial, Consumer<T> out) {
        DataProviderWidget.Factory<T> factory = (DataProviderWidget.Factory<T>) KNOWN_FACTORIES.get(initial.getClass());
        if (factory != null) {
            return new DataProviderScreen<>(title, factory, initial, out);
        } else {
            return null;
        }
    }

    private final DataProviderWidget.Factory<T> factory;
    @Nullable
    private final T initial;
    private final Consumer<T> out;
    private DataProviderWidget<T> provider;

    public DataProviderScreen(
            ITextComponent titleIn, DataProviderWidget.Factory<T> factory, @Nullable T initial, Consumer<T> out
    ) {
        super(titleIn);
        this.factory = factory;
        this.initial = initial;
        this.out = out;
    }

    @Override
    protected void init() {
        super.init();
        DataProviderWidget<T> providerTemp = factory.create(initial, 0, 0);
        final int xMin = (width - providerTemp.getWidth()) / 2;
        final int yMin = (height - providerTemp.getHeight()) / 2;
        provider = factory.create(initial, xMin, yMin);
        addButton(provider);
        addButton(new Button(
                xMin, yMin + provider.getHeight(), provider.getWidth(), 20, new TranslationTextComponent(DONE_KEY),
                $ -> closeScreen()
        ));
    }

    @Override
    public void onClose() {
        super.onClose();
        T result = provider.getData();
        if (result != null) {
            out.accept(result);
        }
    }

    @Override
    protected void renderForeground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

    }
}
