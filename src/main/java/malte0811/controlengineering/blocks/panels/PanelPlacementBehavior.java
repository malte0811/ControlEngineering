package malte0811.controlengineering.blocks.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import malte0811.controlengineering.tiles.panels.ControlPanelTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public class PanelPlacementBehavior implements PlacementBehavior<PanelOrientation> {

    @Override
    public PanelOrientation getPlacementData(BlockItemUseContext ctx) {
        Direction top = ctx.getFace();
        Direction front;
        if (top == Direction.UP) {
            front = ctx.getPlacementHorizontalFacing().getOpposite();
        } else if (top == Direction.DOWN) {
            front = ctx.getPlacementHorizontalFacing();
        } else {
            front = PanelOrientation.HORIZONTAL_FRONT;
        }
        return PanelOrientation.get(top, front);
    }

    @Override
    public Pair<PanelOrientation, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
        PanelOrientation orientation = state.get(PanelOrientation.PROPERTY);
        BlockPos offset;
        if (state.get(PanelBlock.IS_BASE)) {
            offset = BlockPos.ZERO;
        } else {
            offset = new BlockPos(orientation.top.getDirectionVec());
        }
        return Pair.of(orientation, offset);
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(PanelOrientation data) {
        return ImmutableList.of(
                BlockPos.ZERO,
                new BlockPos(data.top.getDirectionVec())
        );
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, PanelOrientation data) {
        return CEBlocks.CONTROL_PANEL.get().getDefaultState()
                .with(PanelOrientation.PROPERTY, data)
                .with(PanelBlock.IS_BASE, offset.equals(BlockPos.ZERO));
    }

    @Override
    public boolean isValidAtOffset(
            BlockPos offset,
            BlockState state,
            TileEntity te,
            PanelOrientation data
    ) {
        if (state.getBlock() != CEBlocks.CONTROL_PANEL.get()) {
            return false;
        }
        PanelOrientation orientation = state.get(PanelOrientation.PROPERTY);
        if (orientation != data) {
            return false;
        }
        boolean isBase = state.get(PanelBlock.IS_BASE);
        return BlockPos.ZERO.equals(offset) == isBase;
    }

    @Override
    public void fillTileData(BlockPos offset, TileEntity te, PanelOrientation data, ItemStack item) {
        if (BlockPos.ZERO.equals(offset) && te instanceof ControlPanelTile) {
            CompoundNBT nbt = item.getTag();
            if (nbt == null || nbt.isEmpty()) {
                nbt = te.write(new CompoundNBT());
            }
            ((ControlPanelTile) te).readComponentsAndTransform(nbt, data);
        }
    }
}
