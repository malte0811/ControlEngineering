package malte0811.controlengineering.datagen;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.datagen.manual.CEManualDataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class CEDataGen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent ev) {
        final var generator = ev.getGenerator();
        final var output = generator.getPackOutput();
        final var exHelper = ev.getExistingFileHelper();
        generator.addProvider(true, new BlockstateGenerator(output, exHelper));
        generator.addProvider(true, new ItemModels(output, exHelper));
        generator.addProvider(true, new Recipes(output, ev.getLookupProvider(), exHelper));
        generator.addProvider(true, new LangGenerator(output));
        generator.addProvider(true, new LootGenerator(output, ev.getLookupProvider()));
        generator.addProvider(true, new BlockTagGenerator(output, ev.getLookupProvider(), exHelper));
        generator.addProvider(true, new LootModifierGenerator(output, ev.getLookupProvider()));
        CEManualDataGenerator.addProviders(generator, exHelper);
    }
}
