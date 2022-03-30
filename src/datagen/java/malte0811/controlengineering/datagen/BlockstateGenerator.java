package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.data.models.NongeneratedModels;
import blusunrize.immersiveengineering.data.models.SpecialModelBuilder;
import com.google.common.collect.ImmutableMap;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.bus.BusInterfaceBlock;
import malte0811.controlengineering.blocks.bus.BusRelayBlock;
import malte0811.controlengineering.blocks.bus.LineAccessBlock;
import malte0811.controlengineering.blocks.logic.LogicCabinetBlock;
import malte0811.controlengineering.blocks.logic.LogicWorkbenchBlock;
import malte0811.controlengineering.blocks.panels.PanelBlock;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.panels.PanelDesignerBlock;
import malte0811.controlengineering.blocks.tape.KeypunchBlock;
import malte0811.controlengineering.blocks.tape.SequencerBlock;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.datagen.modelbuilder.DynamicModelBuilder;
import malte0811.controlengineering.datagen.modelbuilder.LogicCabinetBuilder;
import malte0811.controlengineering.datagen.modelbuilder.LogicWorkbenchBuilder;
import malte0811.controlengineering.util.DirectionUtils;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.function.Supplier;

public class BlockstateGenerator extends BlockStateProvider {
    private static final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
            new ModelFile.UncheckedModelFile(new ResourceLocation(Lib.MODID, "block/ie_empty"))
    );
    private final NongeneratedModels nongenerated;

    public BlockstateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ControlEngineering.MODID, exFileHelper);
        this.nongenerated = new NongeneratedModels(gen, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        createRotatedBlock(CEBlocks.BUS_RELAY, obj("bus_relay.obj"), BusRelayBlock.FACING, 90);
        createRotatedBlock(CEBlocks.LINE_ACCESS, obj("line_access.obj"), LineAccessBlock.FACING, 0);
        createRotatedBlock(CEBlocks.RS_REMAPPER, obj("rs_remapper.obj"), LineAccessBlock.FACING, 0);
        createRotatedBlock(CEBlocks.BUS_INTERFACE, obj("bus_interface.obj"), BusInterfaceBlock.FACING, 90);

        panelModel();
        panelCNCModel();
        keypunchModel();
        sequencerModel();
        logicCabinetModel();
        logicWorkbench();
        rotatedWithOffset(
                CEBlocks.PANEL_DESIGNER,
                obj("panel_designer.obj", modLoc("transform/block_half_size")),
                PanelDesignerBlock.Offset.ORIGIN, PanelDesignerBlock.OFFSET,
                PanelDesignerBlock.FACING
        );
    }

    private void logicWorkbench() {
        var mainModel = obj("logic_cabinet/workbench.obj", nongenerated);
        var schematicModel = obj("logic_cabinet/workbench_schematic.obj", nongenerated);
        var combinedModel = models()
                .withExistingParent("logic_workbench", modLoc("transform/block_half_size"))
                .customLoader(LogicWorkbenchBuilder::new)
                .workbenchModel(mainModel)
                .schematicModel(schematicModel)
                .end();
        rotatedWithOffset(
                CEBlocks.LOGIC_WORKBENCH,
                combinedModel,
                LogicWorkbenchBlock.Offset.ORIGIN, LogicWorkbenchBlock.OFFSET,
                LogicWorkbenchBlock.FACING
        );
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
    }

    private void logicCabinetModel() {
        BlockModelBuilder chassis = obj("logic_cabinet/chassis.obj", modLoc("transform/block_half_size"));
        BlockModelBuilder logicModel = models().getBuilder("combined_logic_cabinet")
                .customLoader(CompositeModelBuilder::begin)
                .submodel("static", chassis)
                .submodel("dynamic", models().getBuilder("dynamic_logic_cabinet")
                        .customLoader(LogicCabinetBuilder::begin)
                        .board(obj("logic_cabinet/board.obj"))
                        .tube(obj("logic_cabinet/tube.obj"))
                        .end())
                .end();
        horizontalRotated(
                CEBlocks.LOGIC_CABINET,
                LogicCabinetBlock.FACING,
                logicModel,
                ImmutableMap.of(LogicCabinetBlock.HEIGHT, 0)
        );
        emptyModel(CEBlocks.LOGIC_CABINET, ImmutableMap.of(LogicCabinetBlock.HEIGHT, 1));
        itemModels().getBuilder(ItemModels.name(CEBlocks.LOGIC_CABINET))
                .parent(chassis);
    }

    private void sequencerModel() {
        BlockModelBuilder staticModel = obj("sequencer.obj", mcLoc("block/block"));
        BlockModelBuilder combinedModel = models().getBuilder("combined_sequencer")
                .customLoader(CompositeModelBuilder::begin)
                .submodel("static", staticModel)
                .submodel("dynamic", models().getBuilder("dynamic_sequencer")
                        .customLoader(SpecialModelBuilder.forLoader(ModelLoaders.SEQUENCER_SWITCH))
                        .end())
                .end();
        horizontalRotated(
                CEBlocks.SEQUENCER,
                SequencerBlock.FACING,
                combinedModel
        );
        itemModels().getBuilder(ItemModels.name(CEBlocks.SEQUENCER)).parent(staticModel);
    }

    private void keypunchModel() {
        BlockModelBuilder staticModel = obj("keypunch.obj", modLoc("transform/block_half_size"));
        BlockModelBuilder combinedModel = models().getBuilder("combined_keypunch")
                .customLoader(CompositeModelBuilder::begin)
                .submodel("static", staticModel)
                .submodel("dynamic", models().getBuilder("dynamic_keypunch")
                        .customLoader(SpecialModelBuilder.forLoader(ModelLoaders.KEYPUNCH_SWITCH))
                        .end()
                )
                .end();
        horizontalRotated(
                CEBlocks.KEYPUNCH,
                KeypunchBlock.FACING,
                combinedModel,
                ImmutableMap.of(KeypunchBlock.UPPER, false)
        );
        emptyModel(CEBlocks.KEYPUNCH, ImmutableMap.of(KeypunchBlock.UPPER, true));
        itemModels().getBuilder(ItemModels.name(CEBlocks.KEYPUNCH))
                .parent(staticModel);
    }

    private void panelCNCModel() {
        BlockModelBuilder model = obj("panel_cnc.obj", modLoc("transform/block_half_size"));
        horizontalRotated(CEBlocks.PANEL_CNC, PanelCNCBlock.FACING, model, Map.of(PanelCNCBlock.UPPER, false));
        emptyModel(CEBlocks.PANEL_CNC, Map.of(PanelCNCBlock.UPPER, true));
        itemModels().getBuilder(ItemModels.name(CEBlocks.PANEL_CNC)).parent(model);
    }

    private <T extends Comparable<T>> void rotatedWithOffset(
            RegistryObject<? extends Block> b,
            ModelFile mainModel,
            T baseOffset, Property<T> offsetProp,
            Property<Direction> facing
    ) {
        for (T offset : offsetProp.getPossibleValues()) {
            ModelFile model;
            if (offset == baseOffset) {
                model = mainModel;
            } else {
                model = EMPTY_MODEL.model;
            }
            horizontalRotated(b, facing, model, ImmutableMap.of(offsetProp, offset));
        }
        itemModels().getBuilder(ItemModels.name(b))
                .parent(mainModel);
    }

    private BlockModelBuilder obj(String objFile) {
        return obj(objFile, models());
    }

    private <T extends ModelBuilder<T>> T obj(String objFile, ModelProvider<T> modelProvider) {
        return obj(objFile, mcLoc("block"), modelProvider);
    }

    private BlockModelBuilder obj(String objFile, ResourceLocation parent) {
        return obj(objFile, parent, models());
    }

    private <T extends ModelBuilder<T>>
    T obj(String objFile, ResourceLocation parent, ModelProvider<T> modelProvider) {
        return modelProvider.withExistingParent(objFile.replace('.', '_'), parent)
                .customLoader(OBJLoaderBuilder::begin)
                .modelLocation(addModelsPrefix(modLoc(objFile)))
                .flipV(true)
                .detectCullableFaces(false)
                .end();
    }

    protected void createRotatedBlock(
            Supplier<? extends Block> block, ModelFile model, Property<Direction> facing, int offsetRotX
    ) {
        VariantBlockStateBuilder stateBuilder = getVariantBuilder(block.get());
        for (Direction d : facing.getPossibleValues()) {
            int x;
            int y;
            switch (d) {
                case UP -> {
                    x = 90;
                    y = 0;
                }
                case DOWN -> {
                    x = -90;
                    y = 0;
                }
                default -> {
                    y = getAngle(d);
                    x = 0;
                }
            }
            stateBuilder.partialState()
                    .with(facing, d)
                    .setModels(new ConfiguredModel(model, x + offsetRotX, y, false));
        }
        itemModels().getBuilder(ItemModels.name(block)).parent(model);
    }

    protected int getAngle(Direction dir) {
        return (int) dir.toYRot();
    }

    private void horizontalRotated(RegistryObject<? extends Block> b, Property<Direction> facing, ModelFile model) {
        horizontalRotated(b, facing, model, ImmutableMap.of());
    }

    private void horizontalRotated(
            RegistryObject<? extends Block> b,
            Property<Direction> facing,
            ModelFile model,
            Map<Property<?>, Comparable<?>> additional
    ) {
        for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
            PartialBlockstate partial = getVariantBuilder(b.get())
                    .partialState()
                    .with(facing, d);
            for (var fixedProperty : additional.entrySet()) {
                partial = withUnchecked(partial, fixedProperty.getKey(), fixedProperty.getValue());
            }
            partial.modelForState()
                    .rotationY((int) d.toYRot())
                    .modelFile(model)
                    .addModel();
        }
        itemModels().getBuilder(ItemModels.name(b)).parent(model);
    }

    private void emptyModel(RegistryObject<? extends Block> b, Map<Property<?>, Comparable<?>> additional) {
        var partialState = getVariantBuilder(b.get()).partialState();
        for (var fixedProperty : additional.entrySet()) {
            partialState = withUnchecked(partialState, fixedProperty.getKey(), fixedProperty.getValue());
        }
        partialState.modelForState()
                .modelFile(EMPTY_MODEL.model)
                .addModel();
    }

    private <T extends Comparable<T>>
    PartialBlockstate withUnchecked(PartialBlockstate original, Property<T> prop, Comparable<?> value) {
        return original.with(prop, (T) value);
    }

    private ResourceLocation addModelsPrefix(ResourceLocation in) {
        return new ResourceLocation(in.getNamespace(), "models/" + in.getPath());
    }

}
