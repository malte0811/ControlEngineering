package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent ev) {
        ev.getGenerator().addProvider(new BlockstateGenerator(ev.getGenerator(), ev.getExistingFileHelper()));
        ev.getGenerator().addProvider(new ItemModels(ev));
        ev.getGenerator().addProvider(new Recipes(ev.getGenerator()));
        ev.getGenerator().addProvider(new LangGenerator(ev.getGenerator()));
        ev.getGenerator().addProvider(new ServerFontData(ev.getGenerator(), ev.getExistingFileHelper()));
        ev.getGenerator().addProvider(new BlockLootGenerator(ev.getGenerator()));
    }
}
