package malte0811.controlengineering;

import malte0811.controlengineering.client.ModelLoaders;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.modelbuilder.DynamicModelBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import javax.annotation.Nullable;
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
        getBuilder(name(CEItems.PANEL_TOP))
                .customLoader(DynamicModelBuilder.customLoader(ModelLoaders.PANEL_TOP_ITEM))
                .end();
        CEItems.CLOCK_GENERATORS.forEach((rl, item) -> addItemModel(rl.getPath().replace('_', '/'), item));
    }

    public static String name(Supplier<? extends IItemProvider> item) {
        return item.get().asItem().getRegistryName().getPath();
    }

    private void addItemModel(@Nullable String texture, Supplier<? extends IItemProvider> item) {
        String path = name(item);
        String textureLoc = texture == null ? path : ("item/" + texture);
        withExistingParent(path, mcLoc("item/generated"))
                .texture("layer0", modLoc(textureLoc));
    }
}
