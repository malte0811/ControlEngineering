package malte0811.controlengineering.datagen;

import blusunrize.immersiveengineering.api.IETags;
import malte0811.controlengineering.ControlEngineering;
import malte0811.controlengineering.blocks.CEBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class BlockTagGenerator extends BlockTagsProvider {
    protected BlockTagGenerator(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookup, ControlEngineering.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        hammerAndPickHarvestable(CEBlocks.BUS_INTERFACE);
        hammerAndPickHarvestable(CEBlocks.BUS_RELAY);
        hammerAndPickHarvestable(CEBlocks.LINE_ACCESS);
        hammerAndPickHarvestable(CEBlocks.RS_REMAPPER);
        pickHarvestable(CEBlocks.CONTROL_PANEL);
        pickHarvestable(CEBlocks.PANEL_CNC);
        pickHarvestable(CEBlocks.SCOPE);
        axeHarvestable(CEBlocks.PANEL_DESIGNER);
        pickHarvestable(CEBlocks.KEYPUNCH);
        axeHarvestable(CEBlocks.SEQUENCER);
        pickHarvestable(CEBlocks.LOGIC_CABINET);
        axeHarvestable(CEBlocks.LOGIC_WORKBENCH);
    }

    private void hammerAndPickHarvestable(Supplier<? extends Block> block) {
        pickHarvestable(block);
        tag(IETags.hammerHarvestable).add(block.get());
    }

    private void pickHarvestable(Supplier<? extends Block> block) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
    }

    private void axeHarvestable(Supplier<? extends Block> block) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(block.get());
    }

    @Nonnull
    @Override
    public String getName() {
        return "Block Tags";
    }
}
