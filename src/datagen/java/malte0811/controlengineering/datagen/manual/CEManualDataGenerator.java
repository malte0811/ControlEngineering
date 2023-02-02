package malte0811.controlengineering.datagen.manual;

import malte0811.controlengineering.ControlEngineering;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CEManualDataGenerator {
    public static void addProviders(DataGenerator gen, ExistingFileHelper exHelper) {
        String outputTo = System.getenv("ce_manual_datagen_path");
        if (outputTo == null) {
            ControlEngineering.LOGGER.info("Skipping manual exports since the output directory is not set");
            return;
        }
        try {
            Path mainOutput = Path.of(outputTo);
            Files.createDirectories(mainOutput);
            gen.addProvider(true, new IconGenerator(gen.getPackOutput(), exHelper, mainOutput.resolve("icons")));
        } catch (IOException xcp) {
            throw new RuntimeException(xcp);
        }
    }
}
