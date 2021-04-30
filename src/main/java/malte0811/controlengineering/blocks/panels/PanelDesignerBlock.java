package malte0811.controlengineering.blocks.panels;

import com.mojang.datafixers.util.Unit;
import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import malte0811.controlengineering.gui.panel.PanelDesignScreen;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelDesignerTile;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PanelDesignerBlock extends CEBlock<Unit, PanelDesignerTile> {
    public PanelDesignerBlock() {
        super(
                Properties.create(Material.IRON),
                PlacementBehavior.simple(CEBlocks.PANEL_DESIGNER),
                (state, world, pos) -> VoxelShapes.fullCube(),
                CETileEntities.PANEL_DESIGNER
        );
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(
            @Nonnull BlockState state,
            @Nonnull World worldIn,
            @Nonnull BlockPos pos,
            @Nonnull PlayerEntity player,
            @Nonnull Hand handIn,
            @Nonnull BlockRayTraceResult hit
    ) {
        if (worldIn.isRemote) {
            Minecraft.getInstance().displayGuiScreen(new PanelDesignScreen());
        }
        return ActionResultType.SUCCESS;
    }
}
