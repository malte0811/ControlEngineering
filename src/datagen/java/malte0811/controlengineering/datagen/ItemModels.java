package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.datagen.modelbuilder.DynamicModelBuilder;
import malte0811.controlengineering.items.CEItems;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.util.function.Supplier;

public class ItemModels extends ItemModelProvider {
    public ItemModels(GatherDataEvent ev) {
        super(ev.getGenerator(), ControlEngineering.MODID, ev.getExistingFileHelper());
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
        withExistingParent(name(CEItems.PANEL_TOP), modLoc("transform/panel_top_base"))
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_MODEL))
                .end();
        withExistingParent(ItemModels.name(CEBlocks.CONTROL_PANEL), modLoc("transform/panel_base"))
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_MODEL))
                .end();
        CEItems.CLOCK_GENERATORS.forEach((rl, item) -> addItemModel(rl.getPath().replace('_', '/'), item));
    }

    public static String name(Supplier<? extends ItemLike> item) {
        return item.get().asItem().getRegistryName().getPath();
    }

    private void addItemModel(String texture, Supplier<? extends ItemLike> item) {
        withExistingParent(name(item), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + texture));
    }
}
