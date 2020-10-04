package malte0811.controlengineering.blocks.panels;

import malte0811.controlengineering.blocks.CEBlock;
import malte0811.controlengineering.tiles.CETileEntities;
import malte0811.controlengineering.tiles.panels.PanelTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PanelBlock extends CEBlock<PanelOrientation> {
    public static final BooleanProperty IS_BASE = BooleanProperty.create("base");

    public PanelBlock() {
        super(
                AbstractBlock.Properties.create(Material.IRON)
                        .notSolid()
                        .hardnessAndResistance(2, 6),
                new PanelPlacementBehavior()
        );
    }

    public static PanelTileEntity getBase(World world, BlockState state, BlockPos pos) {
        BlockPos masterPos;
        if (state.get(IS_BASE)) {
            masterPos = pos;
        } else {
            PanelOrientation po = state.get(PanelOrientation.PROPERTY);
            masterPos = pos.offset(po.top, -1);
        }
        TileEntity te = world.getTileEntity(masterPos);
        if (te instanceof PanelTileEntity) {
            return (PanelTileEntity) te;
        } else {
            return null;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(IS_BASE, PanelOrientation.PROPERTY);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return CETileEntities.CONTROL_PANEL.get().create();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
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
        PanelTileEntity te = getBase(worldIn, state, pos);
        if (te != null) {
            return te.onRightClick(player, state);
        } else {
            return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
        }
    }
}
