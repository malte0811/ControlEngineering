package malte0811.controlengineering.blocks.panels;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import malte0811.controlengineering.blockentity.panels.ControlPanelBlockEntity;
import malte0811.controlengineering.blocks.CEBlocks;
import malte0811.controlengineering.blocks.placement.PlacementBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public class PanelPlacementBehavior implements PlacementBehavior<PanelOrientation> {

    @Override
    public PanelOrientation getPlacementData(BlockPlaceContext ctx) {
        Direction top = ctx.getClickedFace();
        Direction front;
        if (top.getAxis().isVertical()) {
            front = ctx.getHorizontalDirection();
        } else {
            front = PanelOrientation.HORIZONTAL_FRONT;
        }
        return PanelOrientation.get(top, front);
    }

    @Override
    public Pair<PanelOrientation, BlockPos> getPlacementDataAndOffset(BlockState state, BlockEntity be) {
        PanelOrientation orientation = state.getValue(PanelOrientation.PROPERTY);
        BlockPos offset;
        if (state.getValue(PanelBlock.IS_BASE)) {
            offset = BlockPos.ZERO;
        } else {
            offset = new BlockPos(orientation.top.getNormal());
        }
        return Pair.of(orientation, offset);
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(PanelOrientation data) {
        return ImmutableList.of(BlockPos.ZERO, new BlockPos(data.top.getNormal()));
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos offset, PanelOrientation data) {
        return CEBlocks.CONTROL_PANEL.get().defaultBlockState()
                .setValue(PanelOrientation.PROPERTY, data)
                .setValue(PanelBlock.IS_BASE, offset.equals(BlockPos.ZERO));
    }

    @Override
    public boolean isValidAtOffset(BlockPos offset, BlockState state, BlockEntity be, PanelOrientation data) {
        if (state.getBlock() != CEBlocks.CONTROL_PANEL.get()) {
            return false;
        }
        PanelOrientation orientation = state.getValue(PanelOrientation.PROPERTY);
        if (orientation != data) {
            return false;
        }
        boolean isBase = state.getValue(PanelBlock.IS_BASE);
        return BlockPos.ZERO.equals(offset) == isBase;
    }

    @Override
    public void fillBEData(BlockPos offset, BlockEntity bEntity, PanelOrientation data, ItemStack item) {
        if (BlockPos.ZERO.equals(offset) && bEntity instanceof ControlPanelBlockEntity panel) {
            CompoundTag nbt = item.getTag();
            if (nbt == null || nbt.isEmpty()) {
                nbt = panel.save(new CompoundTag());
            }
            panel.readComponentsAndTransform(nbt);
            panel.setChanged();
        }
    }
}
