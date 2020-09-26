package malte0811.controlengineering;

import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.models.LoadedModelProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;

public class LoadedModels extends LoadedModelProvider {
    final Map<ResourceLocation, LoadedModelBuilder> backupModels = new HashMap<>();

    public LoadedModels(DataGenerator gen, ExistingFileHelper exHelper) {
        super(gen, ControlEngineering.MODID, "block", exHelper);
    }

    protected void registerModels() {
        super.generatedModels.putAll(this.backupModels);
    }

    public void backupModels() {
        this.backupModels.putAll(super.generatedModels);
    }

    public String getName() {
        return "Loaded models";
    }
}
