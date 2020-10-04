package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockstateGenerator extends BlockStateProvider {
    private final LoadedModels loadedModels;
    private static final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
            new ModelFile.UncheckedModelFile(new ResourceLocation(Lib.MODID, "block/ie_empty"))
    );

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
        panelModel();

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

    private void panelModel() {
        BlockModelBuilder baseModel = models().cubeAll("panel/base", modLoc("block/control_panel"));
        getVariantBuilder(CEBlocks.CONTROL_PANEL.get())
                .partialState()
                .with(PanelBlock.IS_BASE, true)
                .setModels(new ConfiguredModel(baseModel))
                .partialState()
                .with(PanelBlock.IS_BASE, false)
                //TODO replace TER with model? Or maybe VBOs?
                .setModels(EMPTY_MODEL);
    }
}
