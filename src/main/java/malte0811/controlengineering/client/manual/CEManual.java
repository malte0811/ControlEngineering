package malte0811.controlengineering.client.manual;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import net.minecraft.resources.ResourceLocation;

import static malte0811.controlengineering.ControlEngineering.MODID;

public class CEManual {
    public static void initManual() {
        var ieManual = ManualHelper.getManual();
        ieManual.registerSpecialElement(new ResourceLocation(MODID, "panel_component"), PanelComponentElement::new);
        addComponentFormatEntry(ieManual);
    }

    private static void addComponentFormatEntry(ManualInstance ieManual) {
        var ceCategory = ieManual.getRoot().getOrCreateSubnode(new ResourceLocation(MODID, "main"), 100);
        var panelCategory = ceCategory.getOrCreateSubnode(new ResourceLocation(MODID, "panels"));
        ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
        builder.readFromFile(new ResourceLocation(MODID, "panels/panel_format"));
        // TODO add text describing components and their format
        ieManual.addEntry(panelCategory, builder.create(), 1000);
    }
}
