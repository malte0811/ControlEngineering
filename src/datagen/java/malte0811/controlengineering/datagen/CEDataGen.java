package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent ev) {
        ev.getGenerator().addProvider(true, new BlockstateGenerator(ev.getGenerator(), ev.getExistingFileHelper()));
        ev.getGenerator().addProvider(true, new ItemModels(ev));
        ev.getGenerator().addProvider(true, new Recipes(ev.getGenerator()));
        ev.getGenerator().addProvider(true, new LangGenerator(ev.getGenerator()));
        ev.getGenerator().addProvider(true, new ServerFontData(ev.getGenerator(), ev.getExistingFileHelper()));
        ev.getGenerator().addProvider(true, new LootGenerator(ev.getGenerator()));
        ev.getGenerator().addProvider(true, new ComponentCostGenerator(ev.getGenerator()));
        ev.getGenerator().addProvider(true, new BlockTagGenerator(ev.getGenerator(), ev.getExistingFileHelper()));
        ev.getGenerator().addProvider(true, new LootModifierGenerator(ev.getGenerator()));
    }
}
