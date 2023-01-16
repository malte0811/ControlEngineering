package malte0811.controlengineering.items;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.client.model.panel.PanelItemRenderer;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class PanelTopItem extends Item {
    private static final PanelTransform FLAT_PANEL = new PanelTransform(0, 0, PanelOrientation.UP_NORTH);

    public PanelTopItem() {
        super(new Item.Properties());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new PanelItemRenderer(is -> new PanelData(getComponentsOn(is), FLAT_PANEL));
            }
        });
    }

    public static boolean isEmptyPanelTop(ItemStack candidate) {
        if (candidate.getItem() != CEItems.PANEL_TOP.get()) {
            return false;
        }
        return getComponentsOn(candidate).isEmpty();
    }

    private static final String COMPONENTS_KEY = "components";

    public static List<PlacedComponent> getComponentsOn(ItemStack panel) {
        CompoundTag fullNBT = panel.getTag();
        if (fullNBT == null) {
            return ImmutableList.of();
        }
        ListTag componentList = fullNBT.getList(COMPONENTS_KEY, Tag.TAG_COMPOUND);
        return PlacedComponent.readListFromNBT(componentList);
    }

    public static ItemStack createWithComponents(List<PlacedComponent> components) {
        CompoundTag resultTag = new CompoundTag();
        resultTag.put(COMPONENTS_KEY, PlacedComponent.writeListToNBT(components));
        ItemStack resultStack = new ItemStack(CEItems.PANEL_TOP.get(), 1);
        resultStack.setTag(resultTag);
        return resultStack;
    }
}
