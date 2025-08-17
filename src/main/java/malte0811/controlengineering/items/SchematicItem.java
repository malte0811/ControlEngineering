package malte0811.controlengineering.items;

import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.gui.CEContainers;
import malte0811.controlengineering.gui.logic.LogicDesignMenu;
import malte0811.controlengineering.logic.schematic.Schematic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class SchematicItem extends Item implements ISchematicItem {
    public static final String EMPTY_SCHEMATIC = ControlEngineering.MODID + ".gui.schematic.empty";

    public SchematicItem() {
        super(new Item.Properties());
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(
            @Nonnull Level level, Player player, @Nonnull InteractionHand usedHand
    ) {
        var stack = player.getItemInHand(usedHand);
        if (!level.isClientSide()) {
            var schematic = Objects.requireNonNullElseGet(ISchematicItem.getSchematic(stack), Schematic::new);
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, player1) -> new LogicDesignMenu(CEContainers.LOGIC_DESIGN_VIEW.get(), id, schematic),
                    Component.empty()
            ));
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nullable Level level,
            @Nonnull List<Component> tooltipComponents,
            @Nonnull TooltipFlag isAdvanced
    ) {
        var schematic = ISchematicItem.getSchematic(stack);
        if (Schematic.isEmpty(schematic)) {
            tooltipComponents.add(Component.translatable(EMPTY_SCHEMATIC).withStyle(ChatFormatting.GRAY));
        }
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        var name = Component.translatable(this.getDescriptionId(stack));
        var schematic = ISchematicItem.getSchematic(stack);
        if (!Schematic.isEmpty(schematic)) {
            name.append(": " + schematic.getName());
        }
        return name;
    }
}
