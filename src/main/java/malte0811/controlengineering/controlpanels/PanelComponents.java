package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.util.serialization.StringSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PanelComponents {
    private static final Map<ResourceLocation, PanelComponentType<?>> REGISTRY = new HashMap<>();
    private static final Map<String, ResourceLocation> CREATION_KEY = new HashMap<>();
    public static final PanelComponentType<Button> BUTTON = register("button", Button.createCodec(), Button::new);
    public static final PanelComponentType<Indicator> INDICATOR = register(
            "indicator",
            Indicator.createCodec(),
            Indicator::new
    );

    private static <T extends PanelComponent<T>> PanelComponentType<T> register(
            String path, StringSerializer<T> codec, Supplier<T> empty
    ) {
        ResourceLocation nameRL = new ResourceLocation(ControlEngineering.MODID, path);
        Preconditions.checkState(!REGISTRY.containsKey(nameRL));
        PanelComponentType<T> type = new PanelComponentType<>(nameRL, codec, empty);
        REGISTRY.put(nameRL, type);
        CREATION_KEY.put(path, nameRL);
        return type;
    }

    public static PanelComponentType<?> getType(ResourceLocation id) {
        return Preconditions.checkNotNull(REGISTRY.get(id));
    }

    @Nullable
    public static PanelComponentType<?> getType(String id) {
        ResourceLocation name = CREATION_KEY.get(id);
        if (name != null) {
            return getType(name);
        } else {
            return null;
        }
    }
}
