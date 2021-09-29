package malte0811.controlengineering.items;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.model.PanelItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ControlPanelItem extends CEBlockItem<PanelOrientation> {
    public ControlPanelItem(CEBlock<PanelOrientation, ?> blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IItemRenderProperties() {
            private final BlockEntityWithoutLevelRenderer itemRender = new PanelItemRenderer(is -> {
                CompoundTag tag = is.getTag();
                if (tag == null) {
                    tag = new CompoundTag();
                }
                return new PanelData(tag, PanelOrientation.UP_NORTH);
            });

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return itemRender;
            }
        });
    }
}
