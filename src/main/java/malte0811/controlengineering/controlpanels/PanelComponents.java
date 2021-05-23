package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.components.Button;
import malte0811.controlengineering.controlpanels.components.Indicator;
import malte0811.controlengineering.controlpanels.components.Label;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class PanelComponents {
    public static final TypedRegistry<PanelComponentType<?, ?>> REGISTRY = new TypedRegistry<>();
    private static final BiMap<String, ResourceLocation> CREATION_KEY = HashBiMap.create();
    public static final Button BUTTON = register("button", new Button());
    public static final Indicator INDICATOR = register("indicator", new Indicator());
    public static final Label LABEL = register("label", new Label());

    private static <T extends PanelComponentType<?, ?>> T register(String path, T type) {
        ResourceLocation nameRL = new ResourceLocation(ControlEngineering.MODID, path);
        CREATION_KEY.put(path, nameRL);
        return REGISTRY.register(nameRL, type);
    }

    public static PanelComponentType<?, ?> getType(ResourceLocation id) {
        return Preconditions.checkNotNull(REGISTRY.get(id));
    }

    public static String getCreationKey(PanelComponentType<?, ?> type) {
        return CREATION_KEY.inverse().get(type.getRegistryName());
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
