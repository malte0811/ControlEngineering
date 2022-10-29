package malte0811.controlengineering.items;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PCBStackItem extends Item implements ISchematicItem {
    public static final String FOR_USE_IN_KEY = ControlEngineering.MODID + ".gui.useIn";

    public PCBStackItem() {
        super(new Properties().tab(ControlEngineering.ITEM_GROUP).stacksTo(1));
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> out, @Nonnull TooltipFlag advanced
    ) {
        super.appendHoverText(stack, level, out, advanced);
        out.add(useIn(CEBlocks.LOGIC_CABINET));
    }

    public static Component useIn(RegistryObject<? extends ItemLike> block) {
        return Component.translatable(FOR_USE_IN_KEY, block.get().asItem().getDescription())
                .withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab category, @Nonnull NonNullList<ItemStack> items) {
        // NOP
    }

    @Nullable
    public static Pair<Schematic, BusConnectedCircuit> getSchematicAndCircuit(ItemStack stack) {
        if (stack.getItem() != CEItems.PCB_STACK.get()) {
            return null;
        }
        var schematic = ISchematicItem.getSchematic(stack);
        if (schematic == null) {
            return null;
        }
        Optional<BusConnectedCircuit> circuit = SchematicCircuitConverter.toCircuit(schematic);
        if (!circuit.isPresent()) {
            return null;
        }
        return Pair.of(schematic, circuit.get());
    }

    public static ItemStack forSchematic(Schematic schematic) {
        if (SchematicCircuitConverter.toCircuit(schematic).isPresent()) {
            return ISchematicItem.create(CEItems.PCB_STACK, schematic);
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        // Used for disassembly (sneak-r-click on the soldering burner)
        return true;
    }
}
