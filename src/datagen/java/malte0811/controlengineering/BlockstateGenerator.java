package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.tape.TeletypeBlock;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
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
        dummyIIC(CEBlocks.LINE_ACCESS.get());
        dummyIIC(CEBlocks.BUS_INTERFACE.get());
        panelModel();
        horizontalRotated(CEBlocks.TELETYPE.get(), TeletypeBlock.FACING, obj("typewriter.obj"));
        horizontalRotated(CEBlocks.PANEL_CNC.get(), PanelCNCBlock.FACING, obj("panel_cnc.obj"));

        loadedModels.backupModels();
    }

    private ModelFile obj(String objFile) {
        return models()
                .withExistingParent(objFile, mcLoc("block"))
                .customLoader(OBJLoaderBuilder::begin)
                .modelLocation(addModelsPrefix(modLoc(objFile)))
                .flipV(true)
                .detectCullableFaces(false)
                .end();
    }

    private ResourceLocation forgeLoc(String path) {
        return new ResourceLocation("forge", path);
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

    private void horizontalRotated(Block b, Property<Direction> facing, ModelFile model) {
        for (Direction d : Direction.BY_HORIZONTAL_INDEX) {
            getVariantBuilder(b)
                    .partialState()
                    .with(facing, d)
                    .modelForState()
                    .rotationY((int) d.getHorizontalAngle())
                    .modelFile(model)
                    .addModel();
        }
    }

    private ResourceLocation addModelsPrefix(ResourceLocation in) {
        return new ResourceLocation(in.getNamespace(), "models/" + in.getPath());
    }

}
