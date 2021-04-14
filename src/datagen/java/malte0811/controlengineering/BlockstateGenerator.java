package malte0811.controlengineering;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.data.models.ConnectorBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.tape.TeletypeBlock;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.modelbuilder.DynamicModelBuilder;
import malte0811.controlengineering.modelbuilder.LogicCabinetBuilder;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.lang.reflect.Field;
import java.util.Map;

public class BlockstateGenerator extends BlockStateProvider {
    private static final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
            new ModelFile.UncheckedModelFile(new ResourceLocation(Lib.MODID, "block/ie_empty"))
    );

    public BlockstateGenerator(
            DataGenerator gen,
            ExistingFileHelper exFileHelper
    ) {
        super(gen, ControlEngineering.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        dummyIIC(CEBlocks.BUS_RELAY.get());
        dummyIIC(CEBlocks.LINE_ACCESS.get());
        dummyIIC(CEBlocks.BUS_INTERFACE.get());
        panelModel();
        horizontalRotated(CEBlocks.TELETYPE.get(), TeletypeBlock.FACING, obj("typewriter.obj"));
        horizontalRotated(CEBlocks.PANEL_CNC.get(), PanelCNCBlock.FACING, obj("panel_cnc.obj"));
        BlockModelBuilder logicModel = models().getBuilder("combined_logic_cabinet")
                .customLoader(CompositeModelBuilder::begin)
                .submodel("static", obj("logic_cabinet/chassis.obj"))
                .submodel("dynamic", models().getBuilder("dynamic_logic_cabinet")
                        .customLoader(LogicCabinetBuilder::begin)
                        .board(obj("logic_cabinet/board.obj"))
                        .tube(obj("logic_cabinet/tube.obj"))
                        .end())
                .end();
        horizontalRotated(
                CEBlocks.LOGIC_CABINET.get(),
                LogicCabinetBlock.FACING,
                logicModel,
                ImmutableMap.of(LogicCabinetBlock.HEIGHT, 0)
        );
        horizontalRotated(
                CEBlocks.LOGIC_CABINET.get(),
                LogicCabinetBlock.FACING,
                EMPTY_MODEL.model,
                ImmutableMap.of(LogicCabinetBlock.HEIGHT, 1)
        );
        for (LogicWorkbenchBlock.Offset offset : LogicWorkbenchBlock.Offset.values()) {
            ModelFile model;
            if (offset == LogicWorkbenchBlock.Offset.ORIGIN) {
                model = obj("logic_cabinet/workbench.obj");
            } else {
                model = EMPTY_MODEL.model;
            }
            horizontalRotated(
                    CEBlocks.LOGIC_WORKBENCH.get(),
                    LogicWorkbenchBlock.FACING,
                    model,
                    ImmutableMap.of(LogicWorkbenchBlock.OFFSET, offset)
            );
        }
    }

    private BlockModelBuilder obj(String objFile) {
        return models()
                .withExistingParent(objFile.replace('.', '_'), mcLoc("block"))
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
        ModelFile busRelayModel = models().getBuilder("dummy_iic")
                .customLoader(ConnectorBuilder::begin)
                .baseModel(models().getExistingFile(mcLoc("block/dirt")))
                .layers(ImmutableList.of("solid"))
                .end();
        simpleBlock(b, busRelayModel);
    }

    private String getName(RenderState state) {
        try {
            Field f = RenderState.class.getDeclaredField("name");
            f.setAccessible(true);
            return (String) f.get(state);
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }

    private void panelModel() {
        BlockModelBuilder baseModel = models().cubeAll("panel/base", modLoc("block/control_panel"));
        BlockModelBuilder topModel = models().getBuilder("panel/top")
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_MODEL))
                .end();
        getVariantBuilder(CEBlocks.CONTROL_PANEL.get())
                .partialState()
                .with(PanelBlock.IS_BASE, true)
                .setModels(new ConfiguredModel(baseModel))
                .partialState()
                .with(PanelBlock.IS_BASE, false)
                .setModels(new ConfiguredModel(topModel));
        itemModels().getBuilder(ItemModels.name(CEBlocks.CONTROL_PANEL)).parent(topModel);
    }

    private void horizontalRotated(Block b, Property<Direction> facing, ModelFile model) {
        horizontalRotated(b, facing, model, ImmutableMap.of());
    }

    private void horizontalRotated(
            Block b, Property<Direction> facing, ModelFile model, Map<Property<?>, Comparable<?>> additional
    ) {
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            PartialBlockstate partial = getVariantBuilder(b)
                    .partialState()
                    .with(facing, d);
            for (Map.Entry<Property<?>, Comparable<?>> entry : additional.entrySet()) {
                partial = withUnchecked(partial, entry.getKey(), entry.getValue());
            }
            partial.modelForState()
                    .rotationY((int) d.getHorizontalAngle())
                    .modelFile(model)
                    .addModel();
        }
    }

    private <T extends Comparable<T>>
    PartialBlockstate withUnchecked(PartialBlockstate original, Property<T> prop, Comparable<?> value) {
        return original.with(prop, (T) value);
    }

    private ResourceLocation addModelsPrefix(ResourceLocation in) {
        return new ResourceLocation(in.getNamespace(), "models/" + in.getPath());
    }

}
