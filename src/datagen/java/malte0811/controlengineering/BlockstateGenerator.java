package malte0811.controlengineering;

import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import malte0811.controlengineering.blocks.CEBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockstateGenerator extends BlockStateProvider {
    private final LoadedModels loadedModels;

    public BlockstateGenerator(
            DataGenerator gen,
            ExistingFileHelper exFileHelper,
            LoadedModels loadedModels
    ) {
        super(gen, ControlEngineering.MODID, exFileHelper);
        this.loadedModels = loadedModels;
    }

    @Override
    protected void registerStatesAndModels() {
        dummyIIC(CEBlocks.BUS_RELAY.get());
        loadedModels.backupModels();
    }

    private void dummyIIC(Block b) {
        JsonObject baseJson = new JsonObject();
        baseJson.addProperty("parent", mcLoc("block/dirt").toString());
        ModelFile busRelayModel = loadedModels.getBuilder("dummy_iic")
                .loader(ConnectionLoader.LOADER_NAME)
                .additional("base_model", baseJson)
                .additional("layers", ImmutableList.of(RenderType.getSolid().name));
        simpleBlock(b, busRelayModel);
    }
}
