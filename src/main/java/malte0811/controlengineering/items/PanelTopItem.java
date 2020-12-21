package malte0811.controlengineering.items;

import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class PanelTopItem extends Item {
    public PanelTopItem() {
        super(new Item.Properties().group(ControlEngineering.ITEM_GROUP));
    }

    public static boolean isEmptyPanelTop(ItemStack candidate) {
        if (candidate.getItem() != CEItems.PANEL_TOP.get()) {
            return false;
        }
        return getComponentsOn(candidate).isEmpty();
    }

    private static final String COMPONENTS_KEY = "components";

    public static List<PlacedComponent> getComponentsOn(ItemStack panel) {
        CompoundNBT fullNBT = panel.getTag();
        if (fullNBT == null) {
            return ImmutableList.of();
        }
        ListNBT componentList = fullNBT.getList(COMPONENTS_KEY, Constants.NBT.TAG_COMPOUND);
        return PlacedComponent.readListFromNBT(componentList);
    }

    public static ItemStack createWithComponents(List<PlacedComponent> components) {
        CompoundNBT resultTag = new CompoundNBT();
        resultTag.put(COMPONENTS_KEY, PlacedComponent.writeListToNBT(components));
        ItemStack resultStack = new ItemStack(CEItems.PANEL_TOP.get(), 1);
        resultStack.setTag(resultTag);
        return resultStack;
    }
}
