package malte0811.controlengineering.datagen.modelbuilder;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.BiFunction;

public class DynamicModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    BiFunction<T, ExistingFileHelper, DynamicModelBuilder<T>> customLoader(ResourceLocation loader) {
        return (t, h) -> new DynamicModelBuilder<>(loader, t, h);
    }

    public DynamicModelBuilder(
            ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper
    ) {
        super(loaderId, parent, existingFileHelper, false);
    }
}
