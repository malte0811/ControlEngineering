package malte0811.controlengineering.datagen.modelbuilder;

import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import com.google.gson.JsonObject;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.client.model.logic.LogicWorkbenchModel.Loader;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class LogicWorkbenchBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    private NongeneratedModel workbench;
    private NongeneratedModel schematic;

    public LogicWorkbenchBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ModelLoaders.LOGIC_WORKBENCH, parent, existingFileHelper);
    }

    public LogicWorkbenchBuilder<T> workbenchModel(NongeneratedModel model) {
        this.workbench = model;
        return this;
    }

    public LogicWorkbenchBuilder<T> schematicModel(NongeneratedModel model) {
        this.schematic = model;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        var result = super.toJson(json);
        result.add(Loader.WORKBENCH, workbench.toJson());
        result.add(Loader.SCHEMATIC, schematic.toJson());
        return result;
    }
}
