package malte0811.controlengineering.datagen.modelbuilder;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.logic.model.DynamicLogicModelLoader;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Objects;

public class LogicCabinetBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    LogicCabinetBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new LogicCabinetBuilder<>(parent, existingFileHelper);
    }

    private ModelFile tube;
    private ModelFile board;

    private LogicCabinetBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ModelLoaders.LOGIC_CABINET, parent, existingFileHelper);
    }

    public LogicCabinetBuilder<T> tube(ModelFile tubeModel) {
        Preconditions.checkState(this.tube == null);
        this.tube = tubeModel;
        return this;
    }

    public LogicCabinetBuilder<T> board(ModelFile boardModel) {
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
