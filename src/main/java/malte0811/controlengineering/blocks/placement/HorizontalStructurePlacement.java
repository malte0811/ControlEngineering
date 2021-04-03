package malte0811.controlengineering.blocks.placement;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class HorizontalStructurePlacement<T extends Comparable<T>> implements PlacementBehavior<Direction> {
    private final Property<Direction> facingProperty;
    private final Property<T> offsetProperty;
    private final BiMap<T, BlockPos> getBaseOffset;

    public HorizontalStructurePlacement(
            Property<Direction> facingProperty, Property<T> offsetProperty, Function<T, BlockPos> getBaseOffset
    ) {
        this.facingProperty = facingProperty;
        this.offsetProperty = offsetProperty;
        this.getBaseOffset = HashBiMap.create();
        for (T t : offsetProperty.getAllowedValues()) {
            this.getBaseOffset.put(t, getBaseOffset.apply(t));
        }
    }

    public static HorizontalStructurePlacement<Integer> column(
            Property<Direction> facing, Property<Integer> columnHeight
    ) {
        return new HorizontalStructurePlacement<>(facing, columnHeight, BlockPos.ZERO::up);
    }

    @Override
    public Direction getPlacementData(BlockItemUseContext ctx) {
        return ctx.getPlacementHorizontalFacing();
    }

    @Override
    public Pair<Direction, BlockPos> getPlacementDataAndOffset(BlockState state, TileEntity te) {
        T offset = state.get(this.offsetProperty);
        Direction facing = state.get(this.facingProperty);
        return Pair.of(facing, getPhysicalOffset(facing, offset));
    }

    @Override
    public Collection<BlockPos> getPlacementOffsets(Direction data) {
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos p : this.getBaseOffset.values()) {
            list.add(getPhysicalOffset(data, p));
        }
        return list;
    }

    @Override
    public BlockState getStateForOffset(Block owner, BlockPos physicalOffset, Direction data) {
        return owner.getDefaultState()
                .with(facingProperty, data)
                .with(offsetProperty, getBaseOffset.inverse().get(getLogicalOffset(data, physicalOffset)));
    }

    @Override
    public boolean isValidAtOffset(BlockPos physicalOffset, BlockState state, TileEntity te, Direction data) {
        if (!state.hasProperty(facingProperty)
                || !state.hasProperty(offsetProperty)
                || state.get(facingProperty) != data) {
            return false;
        }
        T logical = state.get(offsetProperty);
        BlockPos physical = getPhysicalOffset(data, logical);
        return physicalOffset.equals(physical);
    }

    private BlockPos getPhysicalOffset(Direction facing, T logicalOffset) {
        return getPhysicalOffset(facing, this.getBaseOffset.get(logicalOffset));
    }

    private static BlockPos getPhysicalOffset(Direction facing, BlockPos logicalOffset) {
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, facing);
        PlacementSettings placeSet = new PlacementSettings().setRotation(rot);
        return Template.transformedBlockPos(placeSet, logicalOffset);
    }

    private static BlockPos getLogicalOffset(Direction facing, BlockPos physicalOffset) {
        Rotation rot = DirectionUtils.getRotationBetweenFacings(facing, Direction.NORTH);
        PlacementSettings placeSet = new PlacementSettings().setRotation(rot);
        return Template.transformedBlockPos(placeSet, physicalOffset);
    }
}
