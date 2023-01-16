package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.datagen.modelbuilder.DynamicModelBuilder;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.scope.module.ScopeModules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ItemModels extends ItemModelProvider {
    public ItemModels(PackOutput output, ExistingFileHelper exHelper) {
        super(output, ControlEngineering.MODID, exHelper);
    }

    @Override
    protected void registerModels() {
        addItemModel("tty_tape_clean", CEItems.EMPTY_TAPE);
        addItemModel("tty_tape_punched", CEItems.PUNCHED_TAPE);
        addItemModel("wirecoil_bus", CEItems.BUS_WIRE_COIL);
        addItemModel("logic_pcbs", CEItems.PCB_STACK);
        addItemModel("logic_schematic", CEItems.SCHEMATIC);
        addItemModel("key", CEItems.KEY);
        addItemModel("lock_with_key", CEItems.LOCK);
        addItemModel("crt_tube", CEItems.CRT_TUBE);
        addItemModel("scope_module_case", CEItems.SCOPE_MODULE_CASE);
        addItemModel("analog_scope_module", ScopeModules.ANALOG.item());
        addItemModel("digital_scope_module", ScopeModules.DIGITAL.item());
        withExistingParent(name(CEItems.PANEL_TOP), modLoc("transform/panel_top_base"))
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_MODEL))
                .end();
        withExistingParent(ItemModels.name(CEBlocks.CONTROL_PANEL), modLoc("transform/panel_base"))
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_MODEL))
                .end();
        CEItems.CLOCK_GENERATORS.forEach((rl, item) -> addItemModel("clock/" + rl.getPath(), item));
    }

    public static String name(Supplier<? extends ItemLike> item) {
        return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
    }

    private void addItemModel(String texture, Supplier<? extends ItemLike> item) {
        withExistingParent(name(item), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texture));
    }
}
