package malte0811.controlengineering.controlpanels;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import malte0811.controlengineering.controlpanels.components.*;
import malte0811.controlengineering.util.RLUtils;
import malte0811.controlengineering.util.typereg.TypedRegistry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class PanelComponents {
    public static final TypedRegistry<PanelComponentType<?, ?>> REGISTRY = new TypedRegistry<>();
    private static final BiMap<String, ResourceLocation> CREATION_KEY = HashBiMap.create();
    // Signal generators
    public static final Button BUTTON = register("button", new Button());
    public static final TimedButton TIMED_BUTTON = register("timed_button", new TimedButton());
    public static final ToggleSwitch TOGGLE_SWITCH = register("toggle_switch", new ToggleSwitch());
    public static final CoveredToggleSwitch COVERED_SWITCH = register("covered_switch", new CoveredToggleSwitch());
    public static final Variac VARIAC = register("variac", new Variac());
    public static final Slider SLIDER_HOR = register("slider_hor", new Slider(true));
    public static final Slider SLIDER_VERT = register("slider_vert", new Slider(false));
    public static final KeySwitch KEY_SWITCH = register("key_switch", new KeySwitch());
    // Signal viewers
    public static final Indicator INDICATOR = register("indicator", new Indicator());
    public static final PanelMeter PANEL_METER = register("panel_meter", new PanelMeter());
    // Misc
    public static final Label LABEL = register("label", new Label());

    private static <T extends PanelComponentType<?, ?>> T register(String path, T type) {
        ResourceLocation nameRL = RLUtils.ceLoc(path);
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
