package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PanelComponents {
    public static final TypedRegistry<PanelComponentType<?, ?>> REGISTRY = new TypedRegistry<>();
    private static final Map<String, ResourceLocation> CREATION_KEY = new HashMap<>();
    public static final Button BUTTON = register("button", new Button());
    public static final Indicator INDICATOR = register("indicator", new Indicator());

    private static <T extends PanelComponentType<?, ?>> T register(String path, T type) {
        ResourceLocation nameRL = new ResourceLocation(ControlEngineering.MODID, path);
        CREATION_KEY.put(path, nameRL);
        return REGISTRY.register(nameRL, type);
    }

    public static PanelComponentType<?, ?> getType(ResourceLocation id) {
        return Preconditions.checkNotNull(REGISTRY.get(id));
    }

    @Nullable
    public static PanelComponentType<?, ?> getType(String id) {
        ResourceLocation name = CREATION_KEY.get(id);
        if (name != null) {
            return getType(name);
        } else {
            return null;
        }
    }
}
