package malte0811.controlengineering.blockentity.panels;

import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import com.google.common.collect.ImmutableList;
import malte0811.controlengineering.blockentity.base.CEBlockEntity;
import malte0811.controlengineering.blockentity.base.IExtraDropBE;
import malte0811.controlengineering.blocks.panels.PanelCNCBlock;
import malte0811.controlengineering.blocks.shapes.ListShapes;
import malte0811.controlengineering.blocks.shapes.SelectionShapeOwner;
import malte0811.controlengineering.blocks.shapes.SelectionShapes;
import malte0811.controlengineering.blocks.shapes.SingleShape;
import malte0811.controlengineering.controlpanels.PlacedComponent;
import malte0811.controlengineering.controlpanels.cnc.CNCInstructionParser;
import malte0811.controlengineering.items.CEItems;
import malte0811.controlengineering.items.PanelTopItem;
import malte0811.controlengineering.items.PunchedTapeItem;
import malte0811.controlengineering.util.*;
import malte0811.controlengineering.util.math.MatrixUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static malte0811.controlengineering.util.ShapeUtils.createPixelRelative;

public class PanelCNCBlockEntity extends CEBlockEntity implements SelectionShapeOwner, IExtraDropBE {
    @Nonnull
    private byte[] insertedTape = new byte[0];
    private final CachedValue<byte[], CNCJob> currentJob = new CachedValue<>(
            () -> insertedTape,
            tape -> {
                if (tape.length > 0) {
                    return CNCJob.createFor(CNCInstructionParser.parse(BitUtils.toString(tape)));
                } else {
                    return null;
                }
            },
            Arrays::equals,
            b -> Arrays.copyOf(b, b.length)
    );
    private int currentTicksInJob;
    private boolean hasPanel;
    private boolean failed = false;
    private final List<PlacedComponent> currentPlacedComponents = new ArrayList<>();
    private final List<CapabilityReference<IItemHandler>> neighborInventories = Util.make(
            new ArrayList<>(),
            list -> {
                for (Direction d : DirectionUtils.BY_HORIZONTAL_INDEX) {
                    list.add(CapabilityReference.forNeighbor(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d));
                }
            }
    );

    private final CachedValue<Direction, SelectionShapes> bottomSelectionShapes = new CachedValue<>(
            () -> getBlockState().getValue(PanelCNCBlock.FACING),
            facing -> new ListShapes(
                    Shapes.block(),
                    MatrixUtils.inverseFacing(facing),
                    ImmutableList.of(
                            new SingleShape(createPixelRelative(1, 14, 1, 15, 16, 15), this::panelClick),
                            new SingleShape(createPixelRelative(2, 4, 0, 14, 12, 2), this::tapeClick)
                    ),
                    ctx -> InteractionResult.PASS
            )
    );

    private static final SelectionShapes topSelectionShapes = new SingleShape(
            PanelCNCBlock.UPPER_SHAPE, $ -> InteractionResult.PASS
    );

    public PanelCNCBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private InteractionResult panelClick(UseOnContext ctx) {
        if (level == null) {
            return InteractionResult.PASS;
        }
        if (hasPanel()) {
            if (!hasFinishedJob()) {
                return InteractionResult.FAIL;
            }
            if (!level.isClientSide && ctx.getPlayer() != null) {
                ItemStack result = PanelTopItem.createWithComponents(currentPlacedComponents);
                ItemUtil.giveOrDrop(ctx.getPlayer(), result);
                hasPanel = false;
                currentPlacedComponents.clear();
                currentTicksInJob = 0;
                failed = false;
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
            return InteractionResult.SUCCESS;
        } else {
            ItemStack heldItem = ctx.getItemInHand();
            if (PanelTopItem.isEmptyPanelTop(heldItem)) {
                if (!level.isClientSide) {
                    hasPanel = true;
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                    Player player = ctx.getPlayer();
                    if (player == null || !player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    private InteractionResult tapeClick(UseOnContext ctx) {
        if (isJobRunning()) {
            return InteractionResult.FAIL;
        }
        final ItemStack held = ctx.getItemInHand();
        if (insertedTape.length == 0) {
            if (CEItems.PUNCHED_TAPE.get() == held.getItem()) {
                if (!level.isClientSide) {
                    insertedTape = PunchedTapeItem.getBytes(held);
                    held.shrink(1);
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                    setChanged();
                }
                return InteractionResult.SUCCESS;
            }
        } else if (!hasPanel()) {
            if (!level.isClientSide) {
                ItemStack result = PunchedTapeItem.withBytes(insertedTape);
                insertedTape = new byte[0];
                ItemUtil.giveOrDrop(ctx.getPlayer(), result);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
                setChanged();
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public void tick() {
        if (isJobRunning()) {
            ++currentTicksInJob;
            int nextComponent = currentPlacedComponents.size();
            CNCJob job = currentJob.get();
            if (nextComponent < job.getTotalComponents()) {
                if (!level.isClientSide && currentTicksInJob >= job.tickPlacingComponent().getInt(nextComponent)) {
                    PlacedComponent componentToPlace = job.components().get(nextComponent);
                    if (!ItemUtil.tryConsumeItemsFrom(
                            componentToPlace.getComponent().getType().getCost(), neighborInventories
                    )) {
                        failed = true;
                    } else {
                        currentPlacedComponents.add(componentToPlace);
                    }
                    BEUtil.markDirtyAndSync(this);
                }
            }
        }
    }

    private boolean hasFinishedJob() {
        CNCJob job = currentJob.get();
        return job != null && (failed || currentTicksInJob >= job.totalTicks());
    }

    private boolean isJobRunning() {
        CNCJob job = currentJob.get();
        return hasPanel && job != null && !failed && currentTicksInJob < job.totalTicks();
    }

    @Override
    public SelectionShapes getShape() {
        if (getBlockState().getValue(PanelCNCBlock.HEIGHT) == 0) {
            return bottomSelectionShapes.get();
        } else {
            return topSelectionShapes;
        }
    }

    @Nullable
    public CNCJob getCurrentJob() {
        return currentJob.get();
    }

    public int getTapeLength() {
        return insertedTape.length;
    }

    public int getCurrentTicksInJob() {
        return currentTicksInJob;
    }

    public List<PlacedComponent> getCurrentPlacedComponents() {
        return currentPlacedComponents;
    }

    public boolean hasPanel() {
        return hasPanel;
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        super.saveAdditional(compound);
        writeSyncedData(compound);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        readSyncedData(nbt);
    }

    @Override
    protected void readSyncedData(CompoundTag compound) {
        insertedTape = compound.getByteArray("tape");
        currentTicksInJob = compound.getInt("currentTick");
        hasPanel = compound.getBoolean("hasPanel");
        failed = compound.getBoolean("failed");
        currentPlacedComponents.clear();
        CNCJob job = currentJob.get();
        if (job != null) {
            int numComponents = 0;
            final int lastTickToConsider = currentTicksInJob - (failed ? 1 : 0);
            while (numComponents < job.getTotalComponents() &&
                    job.tickPlacingComponent().getInt(numComponents) <= lastTickToConsider) {
                currentPlacedComponents.add(job.components().get(numComponents));
                ++numComponents;
            }
        }
    }

    @Override
    protected void writeSyncedData(CompoundTag in) {
        in.putByteArray("tape", insertedTape);
        in.putInt("currentTick", currentTicksInJob);
        in.putBoolean("hasPanel", hasPanel);
        in.putBoolean("failed", failed);
    }

    public boolean hasFailed() {
        return failed;
    }

    @Override
    public void getExtraDrops(Consumer<ItemStack> dropper) {
        if (insertedTape.length > 0) {
            dropper.accept(PunchedTapeItem.withBytes(insertedTape));
        }
        if (hasPanel) {
            dropper.accept(PanelTopItem.createWithComponents(currentPlacedComponents));
        }
    }

    private final CachedValue<BlockPos, AABB> renderBB = new CachedValue<>(
            () -> worldPosition, pos -> new AABB(
            pos.getX(), pos.getY(), pos.getZ(),
            pos.getX() + 1, pos.getY() + 2, pos.getZ() + 2
    ));

    @Override
    public AABB getRenderBoundingBox() {
        return renderBB.get();
    }
}
