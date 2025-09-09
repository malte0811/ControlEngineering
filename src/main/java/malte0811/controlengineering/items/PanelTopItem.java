package malte0811.controlengineering.items;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.client.model.panel.PanelItemRenderer;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.itemdata.CEDataComponents;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PanelTopItem extends Item {
    private static final PanelTransform FLAT_PANEL = new PanelTransform(0, 0, PanelOrientation.UP_NORTH);

    public PanelTopItem() {
        super(new Item.Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final Supplier<BlockEntityWithoutLevelRenderer> renderer = Suppliers.memoize(
                    () -> new PanelItemRenderer(is -> new PanelData(getComponentsOn(is), FLAT_PANEL))
            );

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        });
    }

    public static boolean isEmptyPanelTop(ItemStack candidate) {
        if (candidate.getItem() != CEItems.PANEL_TOP.get()) {
            return false;
        }
        return getComponentsOn(candidate).isEmpty();
    }

    public static List<PlacedComponent> getComponentsOn(ItemStack panel) {
        return panel.getOrDefault(CEDataComponents.PANEL_COMPONENTS, List.of());
    }

    public static ItemStack createWithComponents(List<PlacedComponent> components) {
        ItemStack resultStack = CEItems.PANEL_TOP.toStack();
        resultStack.set(CEDataComponents.PANEL_COMPONENTS, components);
        return resultStack;
    }
}
