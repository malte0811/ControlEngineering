package malte0811.controlengineering.datagen.modelbuilder;


import blusunrize.immersiveengineering.data.models.NongeneratedModels;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.client.model.CacheableCompositeModel;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.ArrayList;
import java.util.List;

public class CacheableCompositeBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    CacheableCompositeBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new CacheableCompositeBuilder<>(parent, existingFileHelper);
    }

    private final List<NongeneratedModels.NongeneratedModel> submodels = new ArrayList<>();

    private CacheableCompositeBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ModelLoaders.CACHED_COMPOSITE, parent, existingFileHelper);
    }

    public CacheableCompositeBuilder<T> submodel(NongeneratedModels.NongeneratedModel model) {
        this.submodels.add(model);
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        JsonArray submodelJson = new JsonArray();
        for (var nongenerated : submodels) {
            submodelJson.add(nongenerated.toJson());
        }
        json.add(CacheableCompositeModel.Loader.SUBMOCELS, submodelJson);
        return json;
    }
}
