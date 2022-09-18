package malte0811.controlengineering.datagen.modelbuilder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.client.model.scope.ScopeModelLoader;
import malte0811.controlengineering.scope.module.ScopeModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class ScopeModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    ScopeModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new ScopeModelBuilder<>(parent, existingFileHelper);
    }

    private ModelBuilder<?> main;
    private final Map<ResourceLocation, ModelBuilder<?>> modules = new HashMap<>();

    private ScopeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ModelLoaders.SCOPE, parent, existingFileHelper);
    }

    public ScopeModelBuilder<T> main(ModelBuilder<?> mainModel) {
        Preconditions.checkState(this.main == null);
        this.main = mainModel;
        return this;
    }

    public ScopeModelBuilder<T> module(ScopeModule<?> module, ModelBuilder<?> model) {
        final var moduleName = module.getRegistryName();
        Preconditions.checkState(!modules.containsKey(moduleName));
        modules.put(moduleName, model);
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        JsonObject moduleModels = new JsonObject();
        for (final var entry : modules.entrySet()) {
            moduleModels.add(entry.getKey().toString(), entry.getValue().toJson());
        }
        json.add(ScopeModelLoader.MAIN_KEY, main.toJson());
        json.add(ScopeModelLoader.MODULES_KEY, moduleModels);
        return json;
    }
}
