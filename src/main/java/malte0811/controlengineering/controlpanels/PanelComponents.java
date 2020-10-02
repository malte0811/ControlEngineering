package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PanelComponents {
    private static final Map<ResourceLocation, PanelComponentType<?>> REGISTRY = new HashMap<>();
    public static PanelComponentType<Button> BUTTON = register("button", Button.createCodec(), Button::new);
    public static PanelComponentType<Indicator> INDICATOR = register("indicator", Indicator.createCodec(), Indicator::new);

    private static <T extends PanelComponent<T>> PanelComponentType<T> register(
            String path,
            Codec<T> codec,
            Supplier<T> empty
        ) {
        ResourceLocation nameRL = new ResourceLocation(ControlEngineering.MODID, path);
        Preconditions.checkState(!REGISTRY.containsKey(nameRL));
        PanelComponentType<T> type = new PanelComponentType<>(nameRL, codec, empty);
        REGISTRY.put(nameRL, type);
        return type;
    }

    public static PanelComponentType<?> getType(ResourceLocation id) {
        return Preconditions.checkNotNull(REGISTRY.get(id));
    }
}
