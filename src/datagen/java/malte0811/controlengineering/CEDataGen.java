package malte0811.controlengineering;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent ev) {
        LoadedModels loadedModels = new LoadedModels(ev.getGenerator(), ev.getExistingFileHelper());
        BlockstateGenerator blockstates = new BlockstateGenerator(
                ev.getGenerator(),
                ev.getExistingFileHelper(),
                loadedModels
        );
        ev.getGenerator().addProvider(blockstates);
        ev.getGenerator().addProvider(new ItemModels(ev));
        ev.getGenerator().addProvider(loadedModels);
    }
}
