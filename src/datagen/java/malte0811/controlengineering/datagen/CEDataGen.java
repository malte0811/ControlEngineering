package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.datagen.manual.CEManualDataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ControlEngineering.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CEDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent ev) {
        final var generator = ev.getGenerator();
        final var output = generator.getPackOutput();
        final var exHelper = ev.getExistingFileHelper();
        generator.addProvider(true, new BlockstateGenerator(output, exHelper));
        generator.addProvider(true, new ItemModels(output, exHelper));
        generator.addProvider(true, new Recipes(output, exHelper));
        generator.addProvider(true, new LangGenerator(output));
        generator.addProvider(true, new LootGenerator(output));
        generator.addProvider(true, new BlockTagGenerator(output, ev.getLookupProvider(), exHelper));
        generator.addProvider(true, new LootModifierGenerator(output));
        CEManualDataGenerator.addProviders(generator, exHelper);
    }
}
