package malte0811.controlengineering.items;

import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.logic.circuit.BusConnectedCircuit;
import malte0811.controlengineering.logic.schematic.Schematic;
import malte0811.controlengineering.logic.schematic.SchematicCircuitConverter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class PCBStackItem extends Item {
    public static final String FOR_USE_IN_KEY = ControlEngineering.MODID + ".gui.useIn";
    private static final String SCHEMATIC_KEY = "schematic";

    public PCBStackItem() {
        super(new Properties().tab(ControlEngineering.ITEM_GROUP));
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> out, @Nonnull TooltipFlag advanced
    ) {
        super.appendHoverText(stack, level, out, advanced);
        out.add(useIn(CEBlocks.LOGIC_CABINET));
    }

    public static Component useIn(RegistryObject<? extends ItemLike> block) {
        return new TranslatableComponent(FOR_USE_IN_KEY, block.get().asItem().getDescription())
                .withStyle(ChatFormatting.GRAY);
    }

    @Nullable
    public static Pair<Schematic, BusConnectedCircuit> getSchematic(ItemStack stack) {
        if (stack.getItem() != CEItems.PCB_STACK.get()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        Schematic schematic = Schematic.CODEC.fromNBT(tag.get(SCHEMATIC_KEY));
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
            ItemStack result = CEItems.PCB_STACK.get().getDefaultInstance();
            result.getOrCreateTag().put(SCHEMATIC_KEY, Schematic.CODEC.toNBT(schematic));
            return result;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
