package malte0811.controlengineering;

import malte0811.controlengineering.controlpanels.model.TopItemModelLoader;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.modelbuilder.DynamicModelBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class ItemModels extends ItemModelProvider {
    public ItemModels(GatherDataEvent ev) {
        super(ev.getGenerator(), ControlEngineering.MODID, ev.getExistingFileHelper());
    }

    @Override
    protected void registerModels() {
        addItemModel("tty_tape_clean", CEItems.EMPTY_TAPE.get());
        addItemModel("tty_tape_punched", CEItems.PUNCHED_TAPE.get());
        getBuilder(name(CEItems.PANEL_TOP.get()))
                .customLoader(DynamicModelBuilder.customLoader(TopItemModelLoader.ID))
                .end();
    }

    private String name(IItemProvider item) {
        return item.asItem().getRegistryName().getPath();
    }

    private void addItemModel(String texture, Item item) {
        String path = name(item);
        String textureLoc = texture == null ? path : ("item/" + texture);
        withExistingParent(path, mcLoc("item/generated"))
                .texture("layer0", modLoc(textureLoc));
    }
}
