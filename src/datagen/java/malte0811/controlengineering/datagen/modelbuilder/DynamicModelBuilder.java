package malte0811.controlengineering.datagen.modelbuilder;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiFunction;

public class DynamicModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>>
    BiFunction<T, ExistingFileHelper, DynamicModelBuilder<T>> customLoader(ResourceLocation loader) {
        return (t, h) -> new DynamicModelBuilder<>(loader, t, h);
    }

    public DynamicModelBuilder(
            ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper
    ) {
        super(loaderId, parent, existingFileHelper);
    }
}
