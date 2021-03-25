package malte0811.controlengineering.modelbuilder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.logic.model.DynamicLogicModelLoader;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class LogicBoxBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    LogicBoxBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new LogicBoxBuilder<>(parent, existingFileHelper);
    }

    private ModelFile tube;
    private ModelFile board;

    private LogicBoxBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ModelLoaders.LOGIC_BOX, parent, existingFileHelper);
    }

    public LogicBoxBuilder<T> tube(ModelFile tubeModel) {
        Preconditions.checkState(this.tube == null);
        this.tube = tubeModel;
        return this;
    }

    public LogicBoxBuilder<T> board(ModelFile boardModel) {
        Preconditions.checkState(this.board == null);
        this.board = boardModel;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        json.addProperty(DynamicLogicModelLoader.BOARD_KEY, Objects.requireNonNull(board).getLocation().toString());
        json.addProperty(DynamicLogicModelLoader.TUBE_KEY, Objects.requireNonNull(tube).getLocation().toString());
        return json;
    }
}
