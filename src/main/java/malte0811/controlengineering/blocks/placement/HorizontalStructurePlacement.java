package malte0811.controlengineering.blocks.placement;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

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
        for (T t : offsetProperty.getPossibleValues()) {
            this.getBaseOffset.put(t, getBaseOffset.apply(t));
        }
    }

    public static HorizontalStructurePlacement<Integer> column(
            Property<Direction> facing, Property<Integer> columnHeight
    ) {
        return new HorizontalStructurePlacement<>(facing, columnHeight, BlockPos.ZERO::above);
    }

    @Override
    public Direction getPlacementData(BlockPlaceContext ctx) {
        return ctx.getHorizontalDirection();
    }

    @Override
    public Pair<Direction, BlockPos> getPlacementDataAndOffset(BlockState state, BlockEntity te) {
        T offset = state.getValue(this.offsetProperty);
        Direction facing = state.getValue(this.facingProperty);
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
        return owner.defaultBlockState()
                .setValue(facingProperty, data)
                .setValue(offsetProperty, getBaseOffset.inverse().get(getLogicalOffset(data, physicalOffset)));
    }

    @Override
    public boolean isValidAtOffset(BlockPos physicalOffset, BlockState state, BlockEntity te, Direction data) {
        if (!state.hasProperty(facingProperty)
                || !state.hasProperty(offsetProperty)
                || state.getValue(facingProperty) != data) {
            return false;
        }
        T logical = state.getValue(offsetProperty);
        BlockPos physical = getPhysicalOffset(data, logical);
        return physicalOffset.equals(physical);
    }

    private BlockPos getPhysicalOffset(Direction facing, T logicalOffset) {
        return getPhysicalOffset(facing, this.getBaseOffset.get(logicalOffset));
    }

    private static BlockPos getPhysicalOffset(Direction facing, BlockPos logicalOffset) {
        Rotation rot = DirectionUtils.getRotationBetweenFacings(Direction.NORTH, facing);
        StructurePlaceSettings placeSet = new StructurePlaceSettings().setRotation(rot);
        return StructureTemplate.calculateRelativePosition(placeSet, logicalOffset);
    }

    private static BlockPos getLogicalOffset(Direction facing, BlockPos physicalOffset) {
        Rotation rot = DirectionUtils.getRotationBetweenFacings(facing, Direction.NORTH);
        StructurePlaceSettings placeSet = new StructurePlaceSettings().setRotation(rot);
        return StructureTemplate.calculateRelativePosition(placeSet, physicalOffset);
    }
}
