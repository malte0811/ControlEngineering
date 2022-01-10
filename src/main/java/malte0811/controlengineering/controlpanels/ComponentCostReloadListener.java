package malte0811.controlengineering.controlpanels;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
//TODO make non-SimpleJson: we care about specific locations rather than everything in one directory
public class ComponentCostReloadListener extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "panel_component_costs";
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    //TODO sync if used by the manual
    public static final Map<ResourceLocation, List<IngredientWithSize>> COMPONENT_COSTS = new HashMap<>();

    public ComponentCostReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(
            @Nonnull Map<ResourceLocation, JsonElement> jsons,
            @Nonnull ResourceManager resourceManager,
            @Nonnull ProfilerFiller profiler
    ) {
        COMPONENT_COSTS.clear();
        for (var componentName : PanelComponents.REGISTRY.getEntries().keySet()) {
            if (!(jsons.get(componentName) instanceof JsonArray costJSON)) {
                continue;//TODO?
            }
            List<IngredientWithSize> totalCost = new ArrayList<>();
            for (var ingredientJSON : costJSON) {
                totalCost.add(IngredientWithSize.deserialize(ingredientJSON));
            }
            COMPONENT_COSTS.put(componentName, totalCost);
        }
    }

    @SubscribeEvent
    public static void registerReloadListeners(AddReloadListenerEvent ev) {
        ev.addListener(new ComponentCostReloadListener());
    }
}
