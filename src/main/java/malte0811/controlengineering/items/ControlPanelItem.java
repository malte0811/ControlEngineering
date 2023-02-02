package malte0811.controlengineering.items;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import com.google.common.base.Suppliers;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.panels.PanelOrientation;
import malte0811.controlengineering.client.model.panel.PanelItemRenderer;
import malte0811.controlengineering.controlpanels.PanelData;
import malte0811.controlengineering.controlpanels.PanelTransform;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControlPanelItem extends CEBlockItem<PanelOrientation> implements IConfigurableTool {
    public static final String FRONT_HEIGHT_OPTION = "front_height";
    public static final String BACK_HEIGHT_OPTION = "back_height";

    public ControlPanelItem(CEBlock<PanelOrientation> blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final Supplier<BlockEntityWithoutLevelRenderer> renderer = Suppliers.memoize(
                    () -> new PanelItemRenderer(is -> getPanelData(is))
            );

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer.get();
            }
        });
    }

    private PanelData getPanelData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
        }
        return new PanelData(tag, PanelOrientation.UP_NORTH);
    }

    @Override
    public boolean canConfigure(ItemStack stack) {
        return true;
    }

    @Override
    public ToolConfig.ToolConfigBoolean[] getBooleanOptions(ItemStack stack) {
        return new ToolConfig.ToolConfigBoolean[0];
    }

    @Override
    public ToolConfig.ToolConfigFloat[] getFloatOptions(ItemStack stack) {
        var transform = getPanelData(stack).getTransform();
        return new ToolConfig.ToolConfigFloat[]{
                new ToolConfig.ToolConfigFloat(FRONT_HEIGHT_OPTION, 60, 20, transform.getFrontHeight()),
                new ToolConfig.ToolConfigFloat(BACK_HEIGHT_OPTION, 60, 40, transform.getBackHeight()),
        };
    }

    @Override
    public void applyConfigOption(ItemStack stack, String key, Object valueObj) {
        // Do not quite allow a height of 0, since then MC decides that we never highlight the block and you can't
        // interact with it. And 1e-3 is close enough.
        var value = Math.max((float) valueObj, 1e-3f);
        var currentTransform = getPanelData(stack).getTransform();
        float frontHeight, backHeight;
        if (FRONT_HEIGHT_OPTION.equals(key)) {
            frontHeight = value;
            backHeight = currentTransform.getBackHeight();
        } else {
            frontHeight = currentTransform.getFrontHeight();
            backHeight = value;
        }
        var newTransform = PanelTransform.withHeights(frontHeight, backHeight, PanelOrientation.UP_NORTH);
        newTransform.addTo(stack.getOrCreateTag());
    }

    @Override
    public String fomatConfigName(ItemStack stack, ToolConfig config) {
        return I18n.get(getKey(config.name));
    }

    @Override
    public String fomatConfigDescription(ItemStack stack, ToolConfig config) {
        // TODO actually describe?
        return fomatConfigName(stack, config);
    }

    public static String getKey(String option) {
        return ControlEngineering.MODID + ".gui.panel_" + option;
    }
}
